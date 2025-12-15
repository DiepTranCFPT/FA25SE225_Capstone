package com.fa25se225.capstone.repository.forum;

import com.fa25se225.capstone.entity.forum.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepository extends JpaRepository<Community, String> {
}
