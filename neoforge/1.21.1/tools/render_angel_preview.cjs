// Run after: bash gradlew exportAnimationPreview --offline
const fs=require('fs');
const path=require('path');
const http=require('http');
const {chromium}=require('playwright');
const root=path.resolve(__dirname,'..');
const directory=path.join(root,'design/angel-animation');
const assets=path.join(root,'src/main/resources/assets/arthousreactions');
const wing=JSON.parse(fs.readFileSync(path.join(assets,'models/effect/angel_wings.json'),'utf8'));
const halo=JSON.parse(fs.readFileSync(path.join(assets,'models/item/angel_halo.json'),'utf8'));
fs.writeFileSync(path.join(directory,'model-data.js'),`window.WING_MODEL=${JSON.stringify(wing)};\nwindow.HALO_MODEL=${JSON.stringify(halo)};\n`);
const server=http.createServer((req,res)=>{
 const file=path.resolve(root,'.'+decodeURIComponent(new URL(req.url,'http://localhost').pathname));
 if(!file.startsWith(root+path.sep)){res.writeHead(403);res.end();return}
 fs.readFile(file,(error,data)=>{if(error){res.writeHead(404);res.end();return}res.setHeader('Content-Type',({'.html':'text/html','.js':'application/javascript','.png':'image/png','.json':'application/json'})[path.extname(file)]||'application/octet-stream');res.end(data)});
});
(async()=>{
 await new Promise(resolve=>server.listen(0,'127.0.0.1',resolve));
 const browser=await chromium.launch({executablePath:process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE,headless:true,args:['--disable-dev-shm-usage']});
 try{
  const page=await browser.newPage({viewport:{width:1200,height:540},deviceScaleFactor:1});
  const errors=[];page.on('pageerror',e=>errors.push(e.message));
  await page.goto(`http://127.0.0.1:${server.address().port}/design/angel-animation/preview.html`);
  await page.evaluate(()=>Promise.race([window.previewReady,new Promise((_,reject)=>setTimeout(()=>reject(new Error('Preview initialization timed out')),20000))]));
  const count=await page.evaluate(()=>ANIMATION_FRAMES.angel.length);
  fs.mkdirSync(path.join(directory,'frames'),{recursive:true});
  for(let i=0;i<count;i++){
   await page.evaluate(ms=>setPreviewTime(ms),i*40);
   await page.screenshot({path:path.join(directory,`frames/${String(i).padStart(3,'0')}.png`)});
  }
  await page.evaluate(()=>setPreviewTime(1000));
  await page.screenshot({path:path.join(directory,'angel-pose.png')});
  if(errors.length)throw new Error(errors.join('; '));
  console.log(`${count} frames exported from actual Java pose samples. No browser errors.`);
 }finally{await browser.close();server.close()}
})().catch(error=>{console.error(error);server.close();process.exitCode=1});
