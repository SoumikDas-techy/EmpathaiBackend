package com.empathai.user.entity;

import com.empathai.user.entity.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
public class Student extends User {

    @Column(name = "school_id")
    private Long schoolId;

    @Column(name = "class_name")
    private String className;

    @Column(name = "section")
    private String section;



    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "parent_email")
    private String parentEmail;

    @Column(name = "roll_no")
    private String rollNo;

    @Column(name = "gender")
    private String gender;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "age")
    private Integer age;

    @Column(name = "parent_name")
    private String parentName;



    public Student(String email, String password, String name) {
        super(email, password, name, UserRole.STUDENT);
    }
}
//need to store phone no.