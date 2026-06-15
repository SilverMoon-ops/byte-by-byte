import React, { useState, useEffect } from 'react'
import axios from "axios"

const Feed = () => {
    const [posts, setPosts] = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        axios.get("http://localhost:3000/posts")
            .then((res) => {
                setPosts(res.data.posts)
            })
            .catch(err => console.error("Error fetching posts:", err))
            .finally(() => setLoading(false))
    }, [])

    if (loading) {
        return (
            <div className="feed-container">
                <div className="empty-state">
                    <h1>Loading feed...</h1>
                </div>
            </div>
        )
    }

    return (
        <section className='feed-container'>
            {posts.length > 0 ? (
                <div className='feed-grid'>
                    {posts.map((post) => (
                        <div key={post._id} className='post-card'>
                            <div className="post-image-container">
                                <img src={post.image} alt={post.caption} loading="lazy" />
                            </div>
                            <div className="post-content">
                                <p className="post-caption">{post.caption}</p>
                                {post.createdAt && (
                                    <p className="post-date">
                                        {new Date(post.createdAt).toLocaleDateString(undefined, {
                                            month: 'short',
                                            day: 'numeric',
                                            year: 'numeric'
                                        })}
                                    </p>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="empty-state">
                    <h1>No posts available. Be the first to post!</h1>
                </div>
            )}
        </section>
    )
}

export default Feed
