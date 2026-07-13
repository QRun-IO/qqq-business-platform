import { chromium } from 'playwright'
import fs from 'node:fs/promises'

await fs.mkdir('visual-qa', { recursive: true })
const browser = await chromium.launch({ headless: true })
const results = []
for (const test of [
  {name:'home-desktop',url:'/',viewport:{width:1440,height:1000},full:true},
  {name:'home-mobile',url:'/',viewport:{width:390,height:844},full:true,mobile:true},
  {name:'blog-desktop',url:'/blog',viewport:{width:1440,height:1000},full:true},
  {name:'login-desktop',url:'/auth/login',viewport:{width:1440,height:1000},full:true},
  {name:'register-mobile',url:'/auth/register',viewport:{width:390,height:844},full:true},
]) {
  const page = await browser.newPage({viewport:test.viewport})
  const errors=[]
  page.on('pageerror',e=>errors.push(`page:${e.message}`))
  page.on('console',m=>{if(m.type()==='error') errors.push(`console:${m.text()}`)})
  const response=await page.goto(`http://localhost:3004${test.url}`,{waitUntil:'networkidle'})
  if(test.mobile){await page.getByRole('button',{name:'Toggle navigation'}).click();await page.waitForTimeout(150)}
  const overflow=await page.evaluate(()=>document.documentElement.scrollWidth>document.documentElement.clientWidth)
  await page.screenshot({path:`visual-qa/${test.name}.png`,fullPage:test.full})
  results.push({name:test.name,status:response?.status(),title:await page.title(),overflow,errors})
  await page.close()
}
const page=await browser.newPage({viewport:{width:1440,height:900}})
await page.goto('http://localhost:3004/',{waitUntil:'networkidle'})
await page.getByRole('textbox',{name:'Work email'}).fill('not-an-email')
await page.getByRole('button',{name:/Join the list/}).click()
const validation=await page.getByText('Enter a valid email address').textContent()
results.push({name:'newsletter-validation',message:validation})
await browser.close()
console.log(JSON.stringify(results,null,2))
