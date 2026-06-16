const express = require("express");
const jwt = require("jsonwebtoken");


const router = express.Router();

router.post("/create", (req, res) => {

    const token = req.cookies.token

    if(!token){
        res.status(401).json({
            message: "Unauthorized"
        })
    }

    try{
    jwt.verify(token, process.env.JWT_SECRET)
    }catch(error){
        return res.status(401).json({
            message: "Token is unvalid"
        })
    }

    res.send("post created successfully")
})



module.exports = router;