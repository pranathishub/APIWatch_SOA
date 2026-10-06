import http from 'node:http';
let status=200, delay=0;
const server=http.createServer((req,res)=>{
  if(req.url==='/control'&&req.method==='POST'){
    let body='';req.on('data',chunk=>{body+=chunk;if(body.length>2048)req.destroy();});
    req.on('end',()=>{try{const values=JSON.parse(body);const nextStatus=Number(values.status??status),nextDelay=Number(values.delay??delay);if(!Number.isInteger(nextStatus)||nextStatus<200||nextStatus>599||!Number.isFinite(nextDelay)||nextDelay<0||nextDelay>30000)throw Error('Invalid settings');status=nextStatus;delay=nextDelay;res.writeHead(200,{'Content-Type':'application/json'});res.end(JSON.stringify({status,delay}));}catch{res.writeHead(400);res.end('Invalid settings');}});
  }else if(req.url==='/health'){
    setTimeout(()=>{res.writeHead(status,{'Content-Type':'application/json'});res.end(JSON.stringify({service:'APIWatch demo target',status}));},delay);
  }else{res.writeHead(404);res.end('Not found');}
});
server.listen(9090,'127.0.0.1',()=>console.log('Demo endpoint: http://127.0.0.1:9090/health'));
