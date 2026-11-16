// Capture the actual Java pose samples without starting Minecraft.
const fs=require('fs'),path=require('path'),http=require('http');
const {chromium}=require('playwright');
const root=path.resolve(__dirname,'..'),output=path.join(root,'design/gesture-polish');
const entries=[['clap','Clap',15,520],['fear','Medo',15,560],['woah','Woah',15,300],
 ['67','67',15,800],['laugh','Risada',25,600],['angel','Anjo',15,900]]
 .filter(entry=>!process.argv[2]||entry[0]===process.argv[2]);
const server=http.createServer((req,res)=>{
 const file=path.resolve(root,'.'+decodeURIComponent(new URL(req.url,'http://localhost').pathname));
 if(!file.startsWith(root+path.sep)){res.writeHead(403);res.end();return}
 fs.readFile(file,(error,data)=>{if(error){res.writeHead(404);res.end();return}
 res.setHeader('Content-Type',({'.html':'text/html','.js':'application/javascript','.png':'image/png'})[path.extname(file)]||'application/octet-stream');res.end(data)});
});
(async()=>{
 await new Promise(resolve=>server.listen(0,'127.0.0.1',resolve));
 const browser=await chromium.launch({executablePath:process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE,headless:true,args:['--disable-dev-shm-usage']});
 try{
  for(const [reaction,name,yaw,poster] of entries){
   const page=await browser.newPage({viewport:{width:1200,height:540},deviceScaleFactor:1});
   const errors=[];page.on('pageerror',error=>errors.push(error.message));
   await page.goto(`http://127.0.0.1:${server.address().port}/design/angel-animation/preview.html?reaction=${reaction}`);
   await page.evaluate(()=>previewReady);
   await page.evaluate(({name,yaw})=>{
    const view=document.querySelector('.view');view.querySelector('.label').textContent=name;
    view.querySelector('.avatar').style.transform=`rotateX(-5deg) rotateY(${yaw}deg)`;
   },{name,yaw});
   const folder=path.join(output,reaction);fs.mkdirSync(folder,{recursive:true});
   const lastFrame=await page.evaluate(reaction=>ANIMATION_FRAMES[reaction].at(-1).ms,reaction);
   for(let index=0;index<70;index++){
    await page.evaluate(ms=>setPreviewTime(ms),Math.min(index*40,lastFrame));
    await page.locator('.view').first().screenshot({path:path.join(folder,`${String(index).padStart(3,'0')}.png`)});
   }
   await page.evaluate(ms=>setPreviewTime(ms),poster);
   await page.locator('.view').first().screenshot({path:path.join(output,`${reaction}-pose.png`)});
   if(errors.length)throw new Error(errors.join('; '));
   await page.close();console.log(`${name}: 70 frames checked, no browser errors.`);
  }
 }finally{await browser.close();server.close()}
})().catch(error=>{console.error(error);server.close();process.exitCode=1});
