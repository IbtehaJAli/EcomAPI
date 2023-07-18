package com.ibtehaj.Ecom.Scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ibtehaj.Ecom.Service.TokenBlacklist;


@Service
public class Swipe {

	 @Autowired
	 private TokenBlacklist tokenBlacklist;


    @Scheduled(cron="0 */6 * * * *") // every six hours
    public void deleteTableData() {
    	List<String> blacklistedTokenValues = tokenBlacklist.getAllTokenValues();
        blacklistedTokenValues.stream().forEach(tokenBlacklist::remove);
        //System.out.println("me working");
    }
}