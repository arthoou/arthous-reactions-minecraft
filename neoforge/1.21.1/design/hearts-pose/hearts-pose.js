// Approval preview only. Nothing in this folder is loaded or packaged by the mod.
const UNIT = 10;
const DURATION_MS = 1850;
const CYCLE_MS = DURATION_MS + 350;
const radians = degrees => degrees * Math.PI / 180;
const smooth = x => { x = Math.max(0, Math.min(1, x)); return x * x * x * (x * (x * 6 - 15) + 10); };

function face(cls, width, height, x, y, transform) {
  return `<i class="face ${cls}" style="width:${width * UNIT}px;height:${height * UNIT}px;background-size:${64 * UNIT}px ${64 * UNIT}px;background-position:${-x * UNIT}px ${-y * UNIT}px;${transform}"></i>`;
}

function cube(width, height, depth, u, v, frontV = v + depth, offset = 0) {
  return `<div class="cube" style="width:${width * UNIT}px;height:${height * UNIT}px;left:${-width * UNIT / 2}px;top:${offset * UNIT}px">`
    + face('front', width, height, u + depth, frontV, `transform:translateZ(${depth * UNIT / 2}px)`)
    + face('back', width, height, u + depth + width + depth, frontV, `transform:translateZ(${-depth * UNIT / 2}px) rotateY(180deg)`)
    + face('right', depth, height, u, frontV, `left:${-depth * UNIT / 2}px;transform:rotateY(-90deg)`)
    + face('left', depth, height, u + depth + width, frontV, `left:${(width - depth / 2) * UNIT}px;transform:rotateY(90deg)`)
    + face('top', width, depth, u + depth, v, `top:${-depth * UNIT / 2}px;transform:rotateX(90deg)`)
    + face('bottom', width, depth, u + depth + width, v, `top:${(height - depth / 2) * UNIT}px;transform:rotateX(-90deg)`)
    + '</div>';
}

function limb(id, u, v, arm) {
  return `<div class="bone" data-bone="${id}">${cube(4, 6, 4, u, v, v + 4, arm ? -2 : 0)}`
    + `<div class="bone" data-joint="${id}" style="top:${(arm ? 4 : 6) * UNIT}px">${cube(4, 6, 4, u, v, v + 10)}</div></div>`;
}

const rigs = [...document.querySelectorAll('.avatar')];
for (const rig of rigs) {
  rig.innerHTML = '<div class="bone" data-upper style="top:200px">'
    + `<div class="bone" style="top:-120px">${cube(8, 12, 4, 16, 16)}</div>`
    + `<div class="bone" data-bone="head">${cube(8, 8, 8, 0, 0, 8, -8)}</div>`
    + limb('rightArm', 40, 16, true) + limb('leftArm', 32, 48, true) + '</div>'
    + limb('rightLeg', 0, 16, false) + limb('leftLeg', 16, 48, false);
  for (let i = 0; i < 3; i++) {
    const heart = document.createElement('img');
    heart.className = 'heart';
    heart.src = '../../src/main/resources/assets/arthousreactions/textures/effect/heart_particle.png';
    heart.dataset.particle = i;
    rig.parentElement.appendChild(heart);
  }
}

function poseAt(ms) {
  const time = ((ms % CYCLE_MS) + CYCLE_MS) % CYCLE_MS;
  const amount = time < 500 ? smooth(time / 500) : time < 1300 ? 1 : 1 - smooth((time - 1300) / 550);
  const breath = Math.sin(time / 1000 * Math.PI * 2) * 0.6 * amount;
  return { time, amount, upperRoll: -3 * amount,
    head: { x: 0, y: -12, pitch: 7 * amount, yaw: -4 * amount, roll: -7 * amount },
    rightArm: { x: -5, y: -10, pitch: -30 * amount + breath, yaw: -27 * amount, roll: 0, bend: -55 * amount - breath },
    leftArm: { x: 5, y: -10, pitch: -30 * amount + breath, yaw: 27 * amount, roll: 0, bend: -55 * amount - breath },
    rightLeg: { x: -1.9, y: 20, pitch: 18 * amount, yaw: 0, roll: 10 * amount, bend: 82 * amount },
    leftLeg: { x: 1.9, y: 20, pitch: 0, yaw: 0, roll: 0, bend: 0 }
  };
}

function draw(ms) {
  const pose = poseAt(ms);
  for (const rig of rigs) {
    rig.querySelector('[data-upper]').style.transform = `rotateZ(${pose.upperRoll}deg)`;
    for (const element of rig.querySelectorAll('[data-bone]')) {
      const bone = pose[element.dataset.bone];
      element.style.transform = `translate3d(${bone.x * UNIT}px,${bone.y * UNIT}px,0) rotateZ(${bone.roll}deg) rotateY(${-bone.yaw}deg) rotateX(${-bone.pitch}deg)`;
      const joint = element.querySelector('[data-joint]');
      if (joint) joint.style.transform = `rotateX(${-bone.bend}deg)`;
    }
    for (const heart of rig.parentElement.querySelectorAll('.heart')) {
      const index = Number(heart.dataset.particle);
      const age = (pose.time - 470 - index * 160) / 850;
      heart.style.opacity = age > 0 && age < 1 ? Math.sin(age * Math.PI) * .85 : 0;
      heart.style.left = `${211 + (index - 1) * 39 + Math.sin(age * 3 + index) * 8}px`;
      heart.style.top = `${229 - age * 110 - index * 10}px`;
      heart.style.transform = `scale(${.65 + age * .25})`;
    }
  }
}

window.poseAt = poseAt;
let fixedTime = null;
window.setPreviewTime = ms => { fixedTime = ms; draw(ms); };
window.playPreview = () => { fixedTime = null; };
const started = performance.now();
function frame(now) { if (fixedTime === null) draw(now - started); requestAnimationFrame(frame); }
requestAnimationFrame(frame);
