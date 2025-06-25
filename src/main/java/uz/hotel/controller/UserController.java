package uz.hotel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uz.hotel.dto.ReservationHistoryDTO;
import uz.hotel.entity.*;
import uz.hotel.entity.enums.ReservationStatus;
import uz.hotel.repository.*;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
    private final HotelDAO hotelDAO;
    private final RoomDAO roomDAO;
    private final ReservationDAO reservationDAO;
    private final UserDAO userDAO;
    private final ReviewDAO reviewDAO;
    private final HistoryDAO historyDAO;

    @GetMapping
    public String showHotels(Model model) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userByEmail = userDAO.getUserByEmail(user.getUsername());
        if (userByEmail.isPresent()) {
            User currentUser = userByEmail.get();
            model.addAttribute("balance", currentUser.getBalance());
        }
        model.addAttribute("hotels", hotelDAO.getAllHotels());
        return "user/user-cabinet";
    }

    @GetMapping("/view-hotel/{id}")
    public String userHome(@PathVariable("id") Long id, Model model) {
        List<Room> rooms = roomDAO.getRoomsByHotelId(id);
        for (Room room : rooms) {
            System.out.println(room);
        }
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userByEmail = userDAO.getUserByEmail(user.getUsername());
        if (userByEmail.isPresent()) {
            User currentUser = userByEmail.get();
            model.addAttribute("balance", currentUser.getBalance());
        }
        model.addAttribute("rooms", rooms);
        return "user/rooms";
    }

    @GetMapping("view-room/{id}")
    public String viewRoomDetail(@PathVariable("id") Long id, Model model) {
        Optional<Room> roomOptional = roomDAO.getRoomById(id);
        if (roomOptional.isEmpty()) {
            model.addAttribute("message", "Room not found");
            return "error";
        }
        Room room = roomOptional.get();
        model.addAttribute("room", room);
        return "user/room-detail";
    }

    @PostMapping("view-room/{id}")
    public String viewRoomDetail(@PathVariable("id") Long id,
                                 @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
                                 @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut,
                                 Model model) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // User user1 = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

//
//        User principal = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (!principal.getId().equals(id)){
//            throw new RuntimeException("you are trying to edit other users data!");
//        }
//        User currentUser = userDao.findById(id).orElseThrow(() -> new RuntimeException("<UNK> <UNK> <UNK> <UNK>."));
//
        Optional<User> userByEmail = userDAO.getUserByEmail(user.getUsername());
        if (userByEmail.isEmpty()) {
            model.addAttribute("message", "User not found");
            return "error";
        }


        User currentUser = userByEmail.get();
        Optional<Room> roomOptional = roomDAO.getRoomByIdAndTime(id, checkIn, checkOut);
        if (roomOptional.isEmpty()) {
            model.addAttribute("message", "Room is already booked during these dates.");
            return "error";
        }


        if (checkIn == null || checkOut == null) {
            model.addAttribute("message", "Check-in and check-out are required.");
            return "error";
        }

        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            model.addAttribute("message", "Check-out must be after check-in.");
            return "error";
        }

        if (checkIn.isBefore(LocalDateTime.now().minusMinutes(1))) {
            model.addAttribute("message", "Check-in time must be in the future.");
            return "error";
        }


        Room room = roomOptional.get();
        Reservation reservation = new Reservation();
        reservation.setUserId(currentUser.getId());
        reservation.setRoomId((long) room.getId());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCheckIn(checkIn);
        reservation.setCheckOut(checkOut);
        reservation.setCreatedAt(LocalDateTime.now());
        long days = Duration.between(checkIn, checkOut).toDays();
        Double totalPrice = (days * room.getPrice());

        if (totalPrice > currentUser.getBalance()) {
            model.addAttribute("message", "User balance not enough to reserve the room");
            return "error";
        }
        reservation.setTotalPrice(totalPrice);
        reservationDAO.save(reservation);
        return "redirect:/user";

    }

    @GetMapping("/my-orders")
    public String showMyOrders(Model model) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userByEmail = userDAO.getUserByEmail(user.getUsername());
        if (userByEmail.isEmpty()) {
            model.addAttribute("message", "User not found");
            return "error";
        }
        User currentUser = userByEmail.get();
        model.addAttribute("orders", roomDAO.getMyOrders(currentUser.getId()));
        return "user/my-orders";
    }


    @PostMapping("/cancel-order/{id}")
    public String cancelOrders(@PathVariable("id") Long id, Model model) {
        reservationDAO.cancelOrderById(id);
        return "redirect:/user";
    }

    @PostMapping("/fill-balance")
    public String fillBalance(@RequestParam("fillBalance") Double balance, Model model) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userByEmail = userDAO.getUserByEmail(user.getUsername());
        if (userByEmail.isEmpty()) {
            model.addAttribute("message", "User not found");
            return "error";
        }
        if (balance <= 0) {
            model.addAttribute("message", "balance should be positive");
            return "error";
        }
        User currentUser = userByEmail.get();
        userDAO.updateUserBalance(currentUser.getId(), balance);
        return "redirect:/user";
    }

    @GetMapping("/user-balance")
    public String showBalance(Model model) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userByEmail = userDAO.getUserByEmail(user.getUsername());
        userByEmail.ifPresent(value -> model.addAttribute("balance", value.getBalance()));
        return "user/user-balance";
    }

    @GetMapping("/user-review")
    public String review(Model model) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userByEmail = userDAO.getUserByEmail(user.getUsername());
        if (userByEmail.isEmpty()) {
            model.addAttribute("message", "User not found");
            return "error";
        }
        User currentUser = userByEmail.get();
        List<Reservation> closedOrders = reservationDAO.getClosedOrders(currentUser.getId());
        List<Hotel> hotels = new ArrayList<>();


        for (Reservation closedOrder : closedOrders) {
            Optional<Hotel> hotelByReservation = hotelDAO.getHotelByReservation(closedOrder);
            hotelByReservation.ifPresent(hotels::add);
        }

        Map<Long, List<Review>> hotelReviewMap = new HashMap<>();
        for (Hotel hotel : hotels) {
            List<Review> reviewsByHotelId = reviewDAO.getReviewsByHotelId((long) hotel.getId());
            hotelReviewMap.put((long) hotel.getId(), reviewsByHotelId);
        }

        model.addAttribute("hotels", hotels);
        model.addAttribute("hotelReviewsMap", hotelReviewMap);
        return "user/user-review";
    }

    @PostMapping("/user-review")
    public String postReview(@RequestParam("hotelId") Long hotelId,
                             @RequestParam("rating") int rating,
                             @RequestParam("comment") String comment,
                             Model model) {
        if(rating == 0){
            model.addAttribute("message", "rating should not be 0");
            return "error";
        }
        if(comment.isEmpty() || comment.isBlank()){
            model.addAttribute("message", "Comment should not be blank or empty");
            return "error";
        }
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userByEmail = userDAO.getUserByEmail(user.getUsername());
        if (userByEmail.isEmpty()) {
            model.addAttribute("message", "User not found");
            return "error";
        }

        User currentUser = userByEmail.get();

        List<Reservation> closedOrders = reservationDAO.getClosedOrders(currentUser.getId());

        Optional<Reservation> matchingReservation = closedOrders.stream()
                .filter(res -> {
                    Long resolvedHotelId = roomDAO.getHotelIdByRoomId(res.getRoomId());
                    return resolvedHotelId != null && resolvedHotelId.equals(hotelId);
                })
                .findFirst();

        if (matchingReservation.isEmpty()) {
            model.addAttribute("message", "No closed reservation found for this hotel.");
            return "error";
        }


        Review review = new Review();
        review.setUserId(currentUser.getId());
        review.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        review.setText(comment);
        review.setRating(rating);
        review.setReservationId((long) matchingReservation.get().getId());
        review.setHotelId(hotelId);

        reviewDAO.save(review);
        reservationDAO.updateStatusAfterComment((long) matchingReservation.get().getId());
        return "redirect:/user/user-review";
    }

    @GetMapping("/user-history")
    public String userHistories(Model model){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userByEmail = userDAO.getUserByEmail(user.getUsername());
        if (userByEmail.isEmpty()) {
            model.addAttribute("message", "User not found");
            return "error";
        }
        User currentUser = userByEmail.get();

        List<ReservationHistoryDTO> allHistories = historyDAO.getAllHistories((long) currentUser.getId());
        model.addAttribute("histories", allHistories);

        return "user/user-history";
    }

}
