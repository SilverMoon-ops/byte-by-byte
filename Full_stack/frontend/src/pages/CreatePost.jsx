import React, { useState } from 'react'
import axios from "axios"
import { useNavigate } from "react-router-dom"

const CreatePost = () => {
    const navigate = useNavigate()
    const [loading, setLoading] = useState(false)
    const [fileName, setFileName] = useState("")

    const handleFileChange = (e) => {
        if (e.target.files && e.target.files[0]) {
            setFileName(e.target.files[0].name)
        }
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setLoading(true)

        const formData = new FormData(e.target)

        axios.post("http://localhost:3000/create-post", formData)
            .then((res) => {
                navigate("/feed")
            })
            .catch((err) => {
                console.log(err)
                alert(err.response?.data?.message || "Error creating post")
            })
            .finally(() => setLoading(false))
    }

    return (
        <section className='create-post-container'>
            <div className='create-post-card'>
                <h1>Create Post</h1>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Image</label>
                        <div className="file-input-wrapper">
                            <div className="btn-file-custom">
                                <span>{fileName || "Click to upload image"}</span>
                            </div>
                            <input 
                                type="file" 
                                name="image" 
                                accept="image/*" 
                                required 
                                onChange={handleFileChange}
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label>Caption</label>
                        <input 
                            className="form-input"
                            type="text" 
                            name='caption' 
                            placeholder='What is on your mind?' 
                            required 
                        />
                    </div>

                    <button className="submit-btn" type='submit' disabled={loading}>
                        {loading ? "Posting..." : "Post Now"}
                    </button>
                </form>
            </div>
        </section>
    )
}

export default CreatePost
