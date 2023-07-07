package com.ibtehaj.Ecom;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class CustomerService {

	private final CustomerProfileRepository customerProfileRepository;
	
	public CustomerService(CustomerProfileRepository customerProfileRepository) {
		this.customerProfileRepository = customerProfileRepository;
	}
	
	public void saveCustomerProfile(CustomerProfile customer) {
		customerProfileRepository.save(customer);
	}
	
	public CustomerProfile getOrCreateCustomerProfile(String customerName, String email, String phone, String address) {
		Optional<CustomerProfile> customerProfile = customerProfileRepository.findByEmail(email);
		if(customerProfile.isPresent()) {
			return customerProfile.get();
		}else {
			CustomerProfile customer = new CustomerProfile(customerName, email, phone, address);
			customerProfileRepository.save(customer);
			return customer;
		}
	}
	
	public CustomerProfile getCustomerProfileById(Long Id) {
		return customerProfileRepository.findById(Id).orElse(null);
	}
	
	public CustomerProfile getCustomerProfileByEmail(String email) {
		return customerProfileRepository.findByEmail(email).orElse(null);
	}
	
	public List<CustomerProfile> getAllCustomerProfiles(){
		return customerProfileRepository.findAll();
	}
	
	public boolean deleteCustomerProfile(String email) {
		Optional<CustomerProfile> optionalCustomerProfile = customerProfileRepository.findByEmail(email);
		if (optionalCustomerProfile.isPresent()) {
			CustomerProfile customer = optionalCustomerProfile.get();
			customerProfileRepository.delete(customer);
			return true;
		}else {
			return false;
		}
	}

}
