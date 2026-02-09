package com.hospital.availability;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
  List<DoctorAvailability> findByDoctorIdAndDayOfWeekAndIsActiveTrueOrderByStartTime(
      Long doctorId, DayOfWeek dayOfWeek);

  List<DoctorAvailability> findByDoctorIdAndIsActiveTrueOrderByDayOfWeekAscStartTimeAsc(Long doctorId);

  void deleteByDoctorId(Long doctorId);

  @Query("""
      select case when count(a) > 0 then true else false end
      from DoctorAvailability a
      where a.doctor.id = :doctorId
        and a.dayOfWeek = :dayOfWeek
        and a.isActive = true
        and :startTime < a.endTime
        and :endTime > a.startTime
      """)
  boolean existsOverlapping(
      @Param("doctorId") Long doctorId,
      @Param("dayOfWeek") DayOfWeek dayOfWeek,
      @Param("startTime") LocalTime startTime,
      @Param("endTime") LocalTime endTime
  );
}
