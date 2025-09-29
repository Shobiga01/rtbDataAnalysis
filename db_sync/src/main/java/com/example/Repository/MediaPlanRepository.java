package com.example.Repository;

import com.example.Model.MediaPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;


@Repository
public interface MediaPlanRepository extends JpaRepository<MediaPlan, BigInteger> {}
