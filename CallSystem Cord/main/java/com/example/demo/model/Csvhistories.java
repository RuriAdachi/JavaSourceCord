package com.example.demo.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "csv_histories")
public class Csvhistories {
	
	   @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "id") 
	    private Integer id;
	 
	 @Column(name = "role_id", nullable = false)
	    private Integer roleId;  
	 
	 @Column(name = "user_id", nullable = false)
	    private Integer userId; 
	 //CSVファイル作成日。サービスクラスでNow指定する
	 @Column(name = "created_date", nullable = false)
	    private LocalDate createdDate;  
	 
	 
	 @Column(name = "call_date")  
	    private LocalDate callDate;


	    public Integer getId() {
	        return id;
	    }

	    public void setId(Integer id) {
	        this.id = id;
	    }
	    
	    public Integer getUserId() {
	 		return userId;
	 	}

	    public void setUserId(Integer userId) {
	        this.userId = userId;
	    }

	 	public Integer getRoleId() {
	 		return roleId;
	 	}

	    public void setRoleId(Integer roleId) {
	        this.roleId = roleId;
	    }

	    public LocalDate getCreatedDate() {
	        return createdDate;
	    }

	    public void setCreatedDate(LocalDate createdDate) {
	        this.createdDate = createdDate;
	    }

	    
	    public LocalDate getCallDate() {
	        return callDate;
	    }

	    public void setCallDate(LocalDate callDate) {
	        this.callDate = callDate;
	    }

}
