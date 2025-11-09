package com.movietime.booking_service.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.movietime.booking_service.DTO.CreateBookingRequest;
import com.movietime.booking_service.DTO.SeatSelection;
import com.movietime.booking_service.Model.Booking;
import com.movietime.booking_service.Model.BookingSeat;
import com.movietime.booking_service.Model.BookingStatus;
import com.movietime.booking_service.Repository.BookingRepository;
import com.movietime.booking_service.Repository.BookingSeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final StringRedisTemplate redisTemplate;

    private static final long LOCK_TTL = 600; // seconds

    public Booking createBooking(CreateBookingRequest req) {
        Long showId = req.getShowId();
        String userId = req.getUserId();

        // 1️ Try locking each seat in Redis
        for (SeatSelection seat : req.getSeats()) {
            String key = "lock:show:" + showId + ":seat:" + seat.getSeatId();
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(key, userId, LOCK_TTL, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(success)) {
                throw new IllegalStateException("Seat already locked or booked: " + seat.getSeatId());
            }
        }

        // 2️ Create Booking record
        Booking booking = Booking.builder()
                .showId(showId)
                .userId(userId)
                .status(BookingStatus.PENDING_PAYMENT)
                .totalAmount(req.getTotalAmount())
                .currency("INR")
                .lockId(UUID.randomUUID().toString())
                .build();
        bookingRepository.save(booking);

        // 3️⃣ Save seat details in booking_seats table
        for (SeatSelection seat : req.getSeats()) {
            BookingSeat seatEntity = BookingSeat.builder()
                    .booking(booking)
                    .seatId(seat.getSeatId())
                    .rowLabel(seat.getRowLabel())
                    .seatNumber(seat.getSeatNumber())
                    .seatType(seat.getSeatType())
                    .price(seat.getPrice())
                    .build();
            bookingSeatRepository.save(seatEntity);
        }
        return booking;
    }
}

