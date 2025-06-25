package uz.hotel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.hotel.dto.HotelDTO;
import uz.hotel.dto.ReservationDTO;
import uz.hotel.entity.Reservation;
import uz.hotel.entity.User;
import uz.hotel.repository.ReservationDAO;
import uz.hotel.repository.UserDAO;
import uz.hotel.service.HotelService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final HotelService hotelService;
    private final UserDAO userDao;
    private final ReservationDAO reservationDAO;

    @GetMapping
    public String adminHome() {
        return "admin/admin-cabinet";
    }

    @GetMapping("/add-hotel")
    public String addHotel() {
        return "admin/add-hotel";
    }
//    @GetMapping("/add-hotel")
//    public String addHotelFragment() {
//        return "admin/fragments/add-hotel :: content";
//    }

    @PostMapping("/add-hotel")
    public String addHotel(@ModelAttribute HotelDTO hotelDTO) {
        hotelService.saveHotel(hotelDTO);
        return "admin/admin-cabinet";
    }

    @GetMapping("/edit-hotel")
    public String editHotelPage(Model model) {
        List<HotelDTO> hotels = hotelService.getAllHotelsWithRooms();
        model.addAttribute("hotels", hotels);
        return "admin/edit-hotels";
    }

//    @GetMapping("/edit-hotel")
//    public String editHotelFragment(Model model) {
//        List<HotelDTO> hotels = hotelService.getAllHotelsWithRooms();
//        model.addAttribute("hotels", hotels);
//        return "admin/fragments/edit-hotels :: content";
//    }


    @PostMapping("/update-hotel")
    public String updateHotel(@ModelAttribute HotelDTO hotelDTO) {
        hotelService.updateHotelWithRooms(hotelDTO);
        return "redirect:/admin/edit-hotel";
    }

    @PostMapping("/delete-hotel")
    public String deleteHotel(@RequestParam("id") int id) {
        hotelService.deleteHotelById(id);
        return "redirect:/admin/edit-hotel";
    }

    @GetMapping("/manage-reservation")
    public String manageReservation(Model model) {
        List<ReservationDTO> reservations = reservationDAO.findAllPendingReservationsWithDetails();
        model.addAttribute("reservations", reservations);
        return "admin/manage-reservation";
    }

//    @GetMapping("/manage-reservation")
//    public String getPendingReservations(Model model) {
//        List<ReservationDTO> reservations = reservationDAO.findAllPendingReservationsWithDetails(); // или другой метод
//        model.addAttribute("reservations", reservations);
//        return "admin/fragments/manage-reservations :: content";
//    }

    @PostMapping("/manage-reservation/update")
    public String updateReservationStatus(@RequestParam("id") int id,
                                          @RequestParam("status") String status,
                                          Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        Optional<User> admin = userDao.getUserByEmail(email);
        if (admin.isEmpty()) {
            model.addAttribute("message", "admin not found");
            return "error";
        }

        Reservation reservation = reservationDAO.findReservationById(id);
        Optional<User> userOpt = reservationDAO.findUserByReservationId(id);

        if (userOpt.isEmpty()) {
            model.addAttribute("message", "user not found");
            return "error";
        }

        User user = userOpt.get();

        // Только если статус ACCEPTED — снимаем деньги
        if (status.equals("PENDING")) {
            if (user.getBalance() < reservation.getTotalPrice()) {
                model.addAttribute("message", "not enough in balance");
                return "error";
            }

            user.setBalance(user.getBalance() - reservation.getTotalPrice());
            userDao.updateUser(user);
        }

        reservationDAO.updateStatus(id, status);
        return "redirect:/admin/manage-reservation";
    }

    @GetMapping("/balance")
    public String balance(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        Optional<User> user = userDao.getUserByEmail(email);
        if (user.isPresent()) {
            model.addAttribute("balance", user.get().getBalance());
        }

        return "admin/balance";
    }

//    @GetMapping("/balance")
//    public String checkBalance(Model model) {
//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        String email = userDetails.getUsername();
//
//        Optional<User> user = userDao.getUserByEmail(email);
//        if (user.isPresent()) {
//            model.addAttribute("balance", user.get().getBalance());
//        }
//
//        return "admin/fragments/check-balance :: content";
//    }

    @GetMapping("/reservations")
    public String reservations(Model model) {
        List<ReservationDTO> reservations = reservationDAO.findAllPendingsAndCancels();
        model.addAttribute("reservationsStatus", reservations);
        return "admin/reservations";
    }

//    @GetMapping("/reservations")
//    public String canceledReservations(Model model) {
//        List<ReservationDTO> reservations = reservationDAO.findAllPendingsAndCancels();
//        model.addAttribute("reservationsStatus", reservations);
//        return "admin/fragments/canceled-reservations :: content";
//    }


    @PostMapping("/refund-money")
    public String refundMoney(@RequestParam("id") int id,
                              @RequestParam("status") String status,
                              Model model) {
        UserDetails userDet = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDet.getUsername();

        Optional<User> admin = userDao.getUserByEmail(email);
        if (admin.isEmpty()) {
            model.addAttribute("message", "admin not found");
            return "error";
        }

        Reservation reservation = reservationDAO.findReservationById(id);
        Optional<User> userOpt = reservationDAO.findUserByReservationId(id);

        if (userOpt.isEmpty()) {
            model.addAttribute("message", "user not found");
            return "error";
        }

        User user = userOpt.get();

        // Только если статус CANCELED_BY_USER — возвращаем деньги
        if(status.equals("CANCELED_BY_USER")){
            double refundToAdmin = admin.get().getBalance() + reservation.getTotalPrice() * 0.1;
            double refundToUser = user.getBalance() + reservation.getTotalPrice() * 0.9;
            user.setBalance(refundToUser);
            admin.get().setBalance(refundToAdmin);
            userDao.updateUser(user);
        }

        reservationDAO.updateStatus(id, status);
        return "redirect:/admin/reservations";
    }

    @GetMapping("/history")
    public String history(Model model) {
        List<ReservationDTO> reservations = reservationDAO.findAll();
        model.addAttribute("allReservations", reservations);
        return "admin/history";
    }

//    @GetMapping("/history")
//    public String reservationHistory(Model model) {
//        List<ReservationDTO> all = reservationDAO.findAll();
//        model.addAttribute("allReservations", all);
//        return "admin/fragments/reservation-history :: content";
//    }


}