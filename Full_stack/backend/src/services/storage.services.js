const Imagekit = require("@imagekit/nodejs");

const imagekit = new Imagekit({
     
    privateKey: process.env.IMAGEKIT_PRIVATE_KEY,
})

async function uploadFile(buffer, fileName){

    const result = await imagekit.files.upload({
        file: buffer.toString("base64"),
        fileName: fileName || "image.png"  
    })
    
    return result;

}

module.exports = uploadFile;