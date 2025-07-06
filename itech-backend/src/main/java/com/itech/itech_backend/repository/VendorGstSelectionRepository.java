package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.model.VendorGstSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorGstSelectionRepository extends JpaRepository<VendorGstSelection, Long> {
    
    List<VendorGstSelection> findByVendorAndGstNumber(User vendor, String gstNumber);
    
    List<VendorGstSelection> findByVendorAndIsSelected(User vendor, boolean isSelected);
    
    List<VendorGstSelection> findByVendor(User vendor);
    
    @Query("SELECT v FROM VendorGstSelection v WHERE v.vendor = :vendor AND v.gstNumber = :gstNumber AND v.isSelected = true")
    List<VendorGstSelection> findSelectedGstRatesByVendorAndGstNumber(@Param("vendor") User vendor, @Param("gstNumber") String gstNumber);
    
    void deleteByVendorAndGstNumber(User vendor, String gstNumber);
}
