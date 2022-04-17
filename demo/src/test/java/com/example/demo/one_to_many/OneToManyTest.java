package com.example.demo.one_to_many;

import com.example.demo.configuration.IntegrationBaseTest;
import com.example.demo.one_to_many.entity.Post;
import com.example.demo.one_to_many.entity.PostComment;
import com.example.demo.one_to_many.entity.unidirectional.UniComment;
import com.example.demo.one_to_many.entity.unidirectional.UniPost;
import com.example.demo.one_to_many.repository.PostCommentRepository;
import com.example.demo.one_to_many.repository.PostRepository;
import com.example.demo.one_to_many.repository.UniPostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

class OneToManyTest extends IntegrationBaseTest {
    @Autowired
    PostRepository postRepository;

    @Autowired
    PostCommentRepository postCommentRepository;

    @Autowired
    UniPostRepository uniPostRepository;

    @Test
    @Transactional
    void should_be_efficient_when_given_bidirectional_one_to_many_mapping() {
        savePost();

        Post savedPost = postRepository.findById(Long.valueOf(1)).get();

        Set<PostComment> commentList = savedPost.getComments();
        commentList.forEach(comment -> System.out.println(comment.toString()));
    }

    @Test
    @Transactional
    void should_create_more_table_when_given_unidirectional_one_to_many_mapping() {
        saveUniPost();

        UniPost savedPost = uniPostRepository.findById(Long.valueOf(1)).get();

        Set<UniComment> commentList = savedPost.getComments();
        commentList.forEach(comment -> System.out.println(comment.toString()));
    }

    public void savePost() {
        Post post = new Post();
        post.setTitle("Title");
        PostComment comment1 = new PostComment();
        comment1.setReview("Review1");
        PostComment comment2 = new PostComment();
        comment2.setReview("Review2");
        post.addComment(comment1);
        post.addComment(comment2);
        postRepository.save(post);
    }

    public void saveUniPost() {
        UniPost post = new UniPost();
        post.setTitle("Title");
        UniComment comment1 = new UniComment();
        comment1.setReview("Review1");
        UniComment comment2 = new UniComment();
        comment2.setReview("Review2");
        post.getComments().add(comment1);
        post.getComments().add(comment2);
        uniPostRepository.save(post);
    }
}
