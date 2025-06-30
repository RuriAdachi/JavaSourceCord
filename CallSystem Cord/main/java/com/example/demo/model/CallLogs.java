package com.example.demo.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "call_logs")
public class CallLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "call_date", nullable = false)
    private LocalDate calldate;  

    @Column(name = "file_name", nullable = false)
    private String filename;

    @Column(name = "file_path", nullable = false)
    private String filepath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt; 

    @Column(name = "status")
    private Integer status;
  
    @Column(name = "is_confirmed")
    private Boolean confirmed;
  //サービスクラスでクエリ文で呼び出すので、今は@Transientは不可能
    @Column(name = "is_admin_confirmed")
    private Boolean adminConfirmed;
    
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item itemid;


    @Transient
    private String yearMonth; 

    // Getters / Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getCalldate() { 
        return calldate;
    }

    public void setCalldate(LocalDate calldate) { 
        this.calldate = calldate;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public LocalDate getCreatedAt() { 
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) { 
        this.createdAt = createdAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    //kakunin
    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }
    
    public Boolean getAdminConfirmed() {
        return adminConfirmed;
    }
    //kannri
    public void setAdminConfirmed(Boolean adminConfirmed) {
        this.adminConfirmed = adminConfirmed;
    }
  
    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }
    
    public Item getItem() {
        return itemid;
    }

    public void setItem(Item itemid) {
        this.itemid = itemid;
    }


  
    //CallLogsをstring型で確認できる
    //debugの時に便利
    @Override
    public String toString() {
        return "CallLogs [id=" + id + ", calldate=" + calldate + ", filename=" + filename +
                ", filepath=" + filepath + ", createdAt=" + createdAt + ", status=" + status + "]";
    }

	
}
