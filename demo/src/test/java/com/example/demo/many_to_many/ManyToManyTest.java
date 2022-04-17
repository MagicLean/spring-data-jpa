package com.example.demo.many_to_many;

import com.example.demo.configuration.IntegrationBaseTest;
import com.example.demo.many_to_many.entity.Course;
import com.example.demo.many_to_many.entity.Student;
import com.example.demo.many_to_many.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

class ManyToManyTest extends IntegrationBaseTest {
    @Autowired
    StudentRepository studentRepository;

    @Test
    @Transactional
    void should_be_efficient_when_given_bidirectional_many_to_many_mapping() {
        Student student = new Student();
        Course course = new Course();
        student.setLikedCourses(Set.of(course));
        studentRepository.save(student);

        Student savedStudent = studentRepository.findById(Long.valueOf(1)).get();
        savedStudent.getLikedCourses().forEach(likedCourse -> System.out.println(likedCourse.toString()));
    }
}
