package com.test.dpg.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.test.dpg.models.EventDetails;

/**
 * @author bardas1
 *
 */
public interface EventDetailsRepository extends JpaRepository<EventDetails, String> {

}
