const express = require('express')
const multer = require('multer')
const path = require('path')

const app = express();
const port = 3000;

//server create
app.listen(port,()=>{
    console.log(`Server is running inport number ${port}`);
});

app.get('/',(req,res)=>{
    res.send('hello this is vishal')
});

//set storage
var storage = multer.diskStorage({
    destination:'./uploads',
    filename:function(req,file,cd){
        cd(null,file.originalname+'-'+Date.now()+path.extname(file.originalname))
    }
});

var upload = multer({
    storage :storage
}).single('video');

//upload fun
app.post('/uploadvideo',(req,res)=>{
    upload(req,res,(err)=>{
        if(err){
            res.send({
                success:err,
                message:err})
        }else{
            console.log(req.file);
            res.send({
                success:'success',
                message:'success'
            });
        }
    });
});