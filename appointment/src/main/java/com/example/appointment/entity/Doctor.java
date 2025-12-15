package com.example.appointment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;
import java.util.HashSet;

@Data
@Entity
@Table(name = "doctors")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }



    @Column(nullable = false)
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    @Column(nullable = false)
    private String title; // 职称
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    @Column
    private String specialty; // 专长
    
    public String getSpecialty() {
        return specialty;
    }
    
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    @Column
    private String introduction; // 简介
    
    public String getIntroduction() {
        return introduction;
    }
    
    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    @Column(name = "photo_url")
    private String photoUrl; // 照片URL
    
    public String getPhotoUrl() {
        return photoUrl;
    }
    
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "doctor_department",
        joinColumns = @JoinColumn(name = "doctor_id"),
        inverseJoinColumns = @JoinColumn(name = "department_id"))
    private Set<Department> departments = new HashSet<>();

    @Column(nullable = false)
    private Integer status;
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Set<Department> getDepartments() {
        return departments;
    }
    
    public void setDepartments(Set<Department> departments) {
        if (departments != null && departments.size() > 3) {
            throw new IllegalArgumentException("医生最多只能关联3个科室");
        }
        this.departments = departments;
    }
    
    public void addDepartment(Department department) {
        if (this.departments.size() >= 3) {
            throw new IllegalArgumentException("医生最多只能关联3个科室");
        }
        this.departments.add(department);
    }
    
    public void removeDepartment(Department department) {
        this.departments.remove(department);
    }

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}