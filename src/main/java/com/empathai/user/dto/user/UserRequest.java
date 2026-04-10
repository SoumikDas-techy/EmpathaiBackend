package com.empathai.user.dto.user;

import com.empathai.user.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Optional — defaults to email in UserService if not provided
    private String username;

    // Optional on update — UserService skips encoding if blank
    private String password;

    private String phoneNumber;

    private String parentEmail;

    @NotNull(message = "Role is required")
    private UserRole role;

    // Role-specific optional fields
    private Long schoolId;

    // Frontend may send school as a name string instead of ID
    private String school;


    private String className;

    private String section;
    private String bloodGroup;

    private String rollNo;

    private String dateOfBirth;
    private Integer age;
    private String gender;
    private String parentName;
    private String address;
}
