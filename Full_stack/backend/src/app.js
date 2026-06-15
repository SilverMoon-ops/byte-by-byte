const express = require('express');
const cors = require('cors');
const multer = require('multer');
const postModel = require("./model/post.model.js");
const uploadFile = require("./services/storage.services.js");

const app = express();
app.use(cors());
app.use(express.json());

const upload = multer({storage: multer.memoryStorage()})


app.post("/create-post", upload.single("image"), async (req, res, next) => {
  try {
    if (!req.file) {
      return res.status(400).json({ message: "Image is required" });
    }
    if (!req.body.caption) {
      return res.status(400).json({ message: "Caption is required" });
    }

    const result = await uploadFile(req.file.buffer, req.file.originalname);
    
    const post = await postModel.create({
      image: result.url,
      caption: req.body.caption
    })

    return res.status(201).json({
      message:"Post created successfully",
      post
    })
  } catch (error) {
    next(error);
  }
});

app.get("/posts", async(req, res, next) => {
  try {
    const posts = await postModel.find().sort({ createdAt: -1 });

    return res.status(200).json({
      message: "Post fetched successfully",
      posts
    })
  } catch (error) {
    next(error);
  }
});

app.get("/health", (req, res) => {
  res.status(200).json({ status: "ok" });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({
    message: "Something went wrong!",
    error: process.env.NODE_ENV === "development" ? err.message : undefined
  });
});

module.exports = app;