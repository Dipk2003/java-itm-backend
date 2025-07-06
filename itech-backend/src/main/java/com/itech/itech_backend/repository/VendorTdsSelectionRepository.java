package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.model.VendorTdsSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorTdsSelectionRepository extends JpaRepository<VendorTdsSelection, Long> {
    
    List<VendorTdsSelection> findByVendorAndPanNumber(User vendor, String panNumber);
    
    List<VendorTdsSelection> findByVendorAndIsSelected(User vendor, boolean isSelected);
    
    List<VendorTdsSelection> findByVendor(User vendor);
    
    @Query("SELECT v FROM VendorTdsSelection v WHERE v.vendor = :vendor AND v.panNumber = :panNumber AND v.isSelected = true")
    List<VendorTdsSelection> findSelectedTdsRatesByVendorAndPanNumber(@Param("vendor") User vendor, @Param("panNumber") String panNumber);
    
    void deleteByVendorAndPanNumber(User vendor, String panNumber);
}
