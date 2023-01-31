package com.example.uhf.Utilities;

import java.util.Date;

public class Tagging {

    private static final long serialVersionUID = 1L;

    private String item_id;
    private String tag_id;
    private String location_id;


    private String location_name;
    private String tag_date;
    private int emp_id;
    private String status;
    private Date inv_date;
    private String   inv_location  ;
    private String   serial_number  ;
    private String   purchase_date  ;
    private String   category_name  ;

    private String   inv_location_name  ;
    private String   inv_status    ;
    private String   title;
    private Boolean updated;
    private String eqpt_no;

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }
    public String getLocation_name() {
        return location_name;
    }
    public String getInv_location_name() {
        return inv_location_name;
    }

    public void setInv_location_name(String inv_location_name) {
        this.inv_location_name = inv_location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }

    public String getTag_date() {
        return tag_date;
    }

    public void setTag_date(String tag_date) {
        this.tag_date = tag_date;
    }

    public int getEmp_id() {
        return emp_id;
    }

    public void setEmp_id(int emp_id) {
        this.emp_id = emp_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getInv_date() {
        return inv_date;
    }

    public void setInv_date(Date inv_date) {
        this.inv_date = inv_date;
    }

    public String getInv_location() {
        return inv_location;
    }

    public void setInv_location(String inv_location) {
        this.inv_location = inv_location;
    }

    public String getInv_status() {
        return inv_status;
    }

    public void setInv_status(String inv_status) {
        this.inv_status = inv_status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getUpdated() {
        return updated;
    }

    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public String getPurchase_date() {
        return purchase_date;
    }

    public void setPurchase_date(String purchase_date) {
        this.purchase_date = purchase_date;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    @Override
    public String toString() {
        return "{" +
                "item_id='" + item_id + '\'' +
                ", tag_id='" + tag_id + '\'' +
                ", location_id='" + location_id + '\'' +
                ", tag_date=" + tag_date +
                ", emp_id='" + emp_id + '\'' +
                ", status='" + status + '\'' +
                ", inv_date=" + inv_date +
                ", inv_location='" + inv_location + '\'' +
                ", inv_status='" + inv_status + '\'' +
                ", title='" + title + '\'' +
                ", updated=" + updated +
                '}';
    }
}
