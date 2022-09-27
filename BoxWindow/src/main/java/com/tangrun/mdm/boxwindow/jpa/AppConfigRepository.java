package com.tangrun.mdm.boxwindow.jpa;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppConfigRepository extends JpaRepositoryImplementation<AppConfig,Long> {




}
