package com.example.eventify.models.users;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BusinessOwnerDTO {
    @SerializedName("id")
    @Expose
    private String id;
    
    @SerializedName("email")
    @Expose
    private String email;
    
    @SerializedName("password")
    @Expose
    private String password;
    
    @SerializedName("address")
    @Expose
    private String address;
    
    @SerializedName("phoneNumber")
    @Expose
    private String phoneNumber;
    
    @SerializedName("profileImage")
    @Expose
    private String profileImage;
    
    @SerializedName("role")
    @Expose
    private RoleDTO role;
    
    @SerializedName("role.name")
    @Expose
    private String roleName;
    
    @SerializedName("suspended")
    @Expose
    private boolean suspended;
    
    @SerializedName("activated")
    @Expose
    private boolean activated;
    
    @SerializedName("name")
    @Expose
    private String name;
    
    @SerializedName("description")
    @Expose
    private String description;
    
    @SerializedName("images")
    @Expose
    private java.util.Set<String> images;

    // Constructors
    public BusinessOwnerDTO() {}

    public BusinessOwnerDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.address = user.getAddress();
        this.phoneNumber = user.getPhoneNumber();
        this.profileImage = user.getProfileImage();
        this.suspended = user.isSuspended();
        this.activated = user.isActivated();
        this.name = user.getName(); // Business name
        this.description = user.getDescription();
        this.images = user.getImages();
        
        // Create role DTO
        this.role = new RoleDTO();
        this.role.setId(user.getRole().getId());
        this.role.setName(user.getRole().getName().toString());
        this.roleName = user.getRole().getName().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public RoleDTO getRole() { return role; }
    public void setRole(RoleDTO role) { this.role = role; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public boolean isSuspended() { return suspended; }
    public void setSuspended(boolean suspended) { this.suspended = suspended; }

    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public java.util.Set<String> getImages() { return images; }
    public void setImages(java.util.Set<String> images) { this.images = images; }
}
