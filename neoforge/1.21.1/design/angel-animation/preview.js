// Exact pose samples exported from Java and the actual converted wing geometry.
const U=9.5;
const reaction=new URLSearchParams(location.search).get('reaction')||'angel';
const frames=ANIMATION_FRAMES[reaction];
const views=[...document.querySelectorAll('.avatar')];
function face(cls,w,h,x,y,style){return `<i class="face ${cls}" style="width:${w*U}px;height:${h*U}px;background-size:${64*U}px ${64*U}px;background-position:${-x*U}px ${-y*U}px;${style}"></i>`}
function cube(w,h,d,u,v,frontV=v+d,offset=0){return `<div class="cube" style="width:${w*U}px;height:${h*U}px;left:${-w*U/2}px;top:${offset*U}px">`+face('front',w,h,u+d,frontV,`transform:translateZ(${d*U/2}px)`)+face('back',w,h,u+d+w+d,frontV,`transform:translateZ(${-d*U/2}px) rotateY(180deg)`)+face('right',d,h,u,frontV,`left:${-d*U/2}px;transform:rotateY(-90deg)`)+face('left',d,h,u+d+w,frontV,`left:${(w-d/2)*U}px;transform:rotateY(90deg)`)+face('top',w,d,u+d,v,`top:${-d*U/2}px;transform:rotateX(90deg)`)+face('bottom',w,d,u+d+w,v,`top:${(h-d/2)*U}px;transform:rotateX(-90deg)`)+`</div>`}
function limb(id,u,v,arm){return `<div class="bone" data-bone="${id}">${cube(4,6,4,u,v,v+4,arm?-2:0)}<div class="bone" data-joint style="top:${(arm?4:6)*U}px">${cube(4,6,4,u,v,v+10)}</div></div>`}
function imageData(path){return new Promise((resolve,reject)=>{const image=new Image();image.onload=()=>{const c=document.createElement('canvas');c.width=image.width;c.height=image.height;c.getContext('2d').drawImage(image,0,0);resolve(c.getContext('2d').getImageData(0,0,c.width,c.height))};image.onerror=reject;image.src=path})}
function meshFace(quad,texture){
 const p=quad.vertices.map(v=>[v[0]*U,v[1]*U,-v[2]*U]);
 const du=p[1].map((v,i)=>v-p[0][i]),dv=p[3].map((v,i)=>v-p[0][i]);
 const width=Math.hypot(...du),height=Math.hypot(...dv);if(width<.001||height<.001)return null;
 const c=document.createElement('canvas');c.className='mesh-face';c.width=Math.max(1,Math.ceil(width*1.5));c.height=Math.max(1,Math.ceil(height*1.5));
 c.style.width=width+'px';c.style.height=height+'px';
 const output=c.getContext('2d').createImageData(c.width,c.height);
 const uv=quad.vertices.map(v=>[v[3],v[4]]);
 for(let y=0;y<c.height;y++)for(let x=0;x<c.width;x++){
  const a=(x+.5)/c.width,b=(y+.5)/c.height;
  const u=uv[0][0]+a*(uv[1][0]-uv[0][0])+b*(uv[3][0]-uv[0][0]);
  const v=uv[0][1]+a*(uv[1][1]-uv[0][1])+b*(uv[3][1]-uv[0][1]);
  const tx=Math.max(0,Math.min(texture.width-1,Math.floor(u*texture.width))),ty=Math.max(0,Math.min(texture.height-1,Math.floor(v*texture.height)));
  const source=(ty*texture.width+tx)*4,target=(y*c.width+x)*4;
  for(let k=0;k<4;k++)output.data[target+k]=texture.data[source+k];
 }
 c.getContext('2d').putImageData(output,0,0);
 const a=du.map(v=>v/width),b=dv.map(v=>v/height),n=[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]];
 c.style.transform=`matrix3d(${[...a,0,...b,0,...n,0,...p[0],1].join(',')})`;
 c.style.filter=`brightness(${.82+.18*Math.abs(quad.normal[1])})`;
 return c;
}
function wingNode(node,texture){
 const element=document.createElement('div');element.className='wing-node';
 const [x,y,z]=node.position,[rx,ry,rz]=node.rotation;
 element.style.transform=`translate3d(${x*U}px,${y*U}px,${-z*U}px) rotateZ(${rz}deg) rotateY(${-ry}deg) rotateX(${-rx}deg) scale3d(${node.scale.join(',')})`;
 let target=element;
 if(node.motion){const p=node.pivot;const move=document.createElement('div');move.className='wing-node';move.dataset.motion=node.motion;move.style.transformOrigin=`${p[0]*U}px ${p[1]*U}px ${-p[2]*U}px`;element.appendChild(move);target=move;}
 for(const quad of node.quads){const face=meshFace(quad,texture);if(face)target.appendChild(face)}
 for(const child of node.children)target.appendChild(wingNode(child,texture));
 return element;
}
function halo(texture){
 const root=document.createElement('div');root.className='halo';
 root.style.transform=`translate3d(0,${-9.28*U}px,0) scale3d(.294,.294,.294)`;
 for(const element of HALO_MODEL.elements){
  const [x,y,z]=element.from.map(v=>v-8),[X,Y,Z]=element.to.map(v=>v-8);
  const faces={east:[[[X,y,Z],[X,y,z],[X,Y,z],[X,Y,Z]],[1,0,0]],west:[[[x,y,z],[x,y,Z],[x,Y,Z],[x,Y,z]],[-1,0,0]],up:[[[X,y,Z],[x,y,Z],[x,y,z],[X,y,z]],[0,-1,0]],down:[[[X,Y,z],[x,Y,z],[x,Y,Z],[X,Y,Z]],[0,1,0]],north:[[[X,y,z],[x,y,z],[x,Y,z],[X,Y,z]],[0,0,-1]],south:[[[x,y,Z],[X,y,Z],[X,Y,Z],[x,Y,Z]],[0,0,1]]};
  for(const [name,face] of Object.entries(element.faces)){
   const [u,v,A,B]=face.uv.map(v=>v/16),[positions,normal]=faces[name],uv=[[A,v],[u,v],[u,B],[A,B]];
   const canvas=meshFace({normal,vertices:positions.map((p,i)=>[...p,...uv[i]])},texture);if(canvas)root.appendChild(canvas);
  }
 }
 return root;
}
window.previewReady=(async()=>{
 const wingTexture=await imageData('../../src/main/resources/assets/arthousreactions/textures/effect/angel_wings.png');
 const haloTexture=await imageData('../../src/main/resources/assets/arthousreactions/textures/item/angel_halo.png');
 for(const rig of views){
  rig.innerHTML=`<div class="bone" data-bone="torso">${cube(8,12,4,16,16)}</div><div class="bone" data-bone="head">${cube(8,8,8,0,0,8,-8)}</div>`+limb('rightArm',40,16,true)+limb('leftArm',32,48,true)+limb('rightLeg',0,16,false)+limb('leftLeg',16,48,false);
  const wings=document.createElement('div');wings.className='wing-node';wings.dataset.wings='';const m=WING_MODEL.mount;
  wings.style.transform=`translate3d(${m[0]*U}px,${m[1]*U}px,${-m[2]*U}px)`;wings.appendChild(wingNode(WING_MODEL.root,wingTexture));
  rig.querySelector('[data-bone="torso"]').appendChild(wings);rig.appendChild(halo(haloTexture));
 }
 window.setPreviewTime(0);
})();
let fixedTime=null;
function draw(ms){
 const index=Math.min(frames.length-1,Math.floor((((ms%(frames.length*40))+frames.length*40)%(frames.length*40))/40));
 const frame=frames[index];
 for(const rig of views){
  for(const element of rig.querySelectorAll('[data-bone]')){
   const part=frame.parts[element.dataset.bone],[x,y,z]=part.position,[rx,ry,rz]=part.rotation;
   element.style.transform=`translate3d(${x*U}px,${y*U}px,${-z*U}px) rotateZ(${rz}rad) rotateY(${-ry}rad) rotateX(${-rx}rad)`;
   const joint=element.querySelector('[data-joint]');if(joint)joint.style.transform=`rotateX(${-part.bend}rad)`;
  }
  rig.querySelector('[data-wings]').style.display=reaction==='angel'&&frame.wings.alpha>0?'block':'none';
  // Opacity on a 3D parent flattens its descendants in Chromium. Fade the actual faces instead.
  for(const plane of rig.querySelectorAll('[data-wings] .mesh-face'))plane.style.opacity=frame.wings.alpha;
  for(const motion of rig.querySelectorAll('[data-motion]'))motion.style.transform=`rotateY(${-frame.wings[motion.dataset.motion]}deg)`;
  rig.querySelector('.halo').style.display=reaction==='angel'&&frame.ms<=2200?'block':'none';
 }
}
window.setPreviewTime=ms=>{fixedTime=ms;draw(ms)};
window.playPreview=()=>{fixedTime=null};
const start=performance.now();
function animate(now){if(fixedTime===null&&views[0].querySelector('[data-bone]'))draw(now-start);requestAnimationFrame(animate)}
requestAnimationFrame(animate);
