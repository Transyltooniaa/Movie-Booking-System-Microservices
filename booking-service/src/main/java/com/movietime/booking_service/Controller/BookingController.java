package com.movietime.booking_service.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movietime.booking_service.DTO.CreateBookingRequest;
import com.movietime.booking_service.Model.Booking;
import com.movietime.booking_service.Service.BookingService;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;
    @PostMapping("/create")
    public ResponseEntity<Booking> createBooking(@RequestBody CreateBookingRequest req) {
        Booking booking = bookingService.createBooking(req);
        return ResponseEntity.ok(booking);
    }
}
