package com.example.eventify.services;

import com.example.eventify.models.enums.ReservationMethod;
import com.example.eventify.models.enums.Status;
import com.example.eventify.models.EventType;
import com.example.eventify.models.Service;
import com.example.eventify.models.SolutionCategory;

import java.util.*;
import java.util.stream.Collectors;

public class ServiceService {

    // The single instance of the ServiceService (Singleton)
    private static ServiceService instance;

    private List<Service> services = new ArrayList<>();
    private int idCounter = 3;

    // Private constructor to prevent instantiation
    private ServiceService() {
        prepareServices();
    }

    // Public static method to get the single instance of the ServiceService
    public static ServiceService getInstance() {
        if (instance == null) {
            synchronized (ServiceService.class) {
                if (instance == null) {
                    instance = new ServiceService();
                }
            }
        }
        return instance;
    }

    private void prepareServices() {
        Service service1 = new Service(
                "Google Pixel 9 Pro",
                new SolutionCategory("phone", "desc", Status.ACCEPTED),
                new HashSet<>(Arrays.asList(
                        new EventType("Photography", "Photography event type", true),
                        new EventType("Gaming", "Gaming event type", true)
                )),
                Status.ACCEPTED,
                "pixel phone",
                1000,
                10,
                new HashSet<>(Arrays.asList(
                        "https://cdn.dxomark.com/wp-content/uploads/medias/post-176677/Google-Pixel-9-Pro-XL_featured-image-packshot-review.png",
                        "https://storage.googleapis.com/gweb-uniblog-publish-prod/images/P9P9PThumbnail_16x9_Opt2_2.width-1300.jpg"
                )),
                true,
                true,
                "pixel",
                30,
                0,
                0,
                14,
                6,
                ReservationMethod.AUTOMATIC
        );
        service1.setId(1);

        Service service2 = new Service(
                "OnePlus 13",
                new SolutionCategory("phone", "desc", Status.ACCEPTED),
                new HashSet<>(Arrays.asList(
                        new EventType("Photography", "Photography event type", true),
                        new EventType("Gaming", "Gaming event type", true)
                )),
                Status.ACCEPTED,
                "oneplus phone",
                2000,
                10,
                new HashSet<>(Arrays.asList(
                        "https://images.indianexpress.com/2024/09/oneplus-13.jpg",
                        "https://heyupnow.com/cdn/shop/files/Oneplus13_600x600.png?v=1729846188"
                )),
                true,
                true,
                "oneplus",
                0,
                1,
                10,
                14,
                7,
                ReservationMethod.AUTOMATIC
        );
        service2.setId(2);

        Service service3 = new Service(
                "Oppo Find X8 Pro",
                new SolutionCategory("phone", "desc", Status.ACCEPTED),
                new HashSet<>(Arrays.asList(
                        new EventType("Gaming", "Gaming event type", true),
                        new EventType("Photography", "Photography event type", true)
                )),
                Status.ACCEPTED,
                "oppo phone",
                5000,
                10,
                new HashSet<>(Arrays.asList(
                        "https://www.pc-tablet.co.in/wp-content/uploads/2024/10/Oppo-Find-X8-Ultra-Camera-Specs-Revealed-in-New-Leak.jpg",
                        "https://www.notebookcheck.net/fileadmin/Notebooks/News/_nc4/find-x8-pro-bottom92.jpg"
                )),
                true,
                true,
                "oppo",
                30,
                0,
                0,
                14,
                8,
                ReservationMethod.AUTOMATIC
        );
        service3.setId(3);

        services.add(service1);
        services.add(service2);
        services.add(service3);
    }

    public Service create(Service service) {
        idCounter++;
        service.setId(idCounter);
        services.add(service);
        return service;
    }

    public Service get(int id) {
        return services.stream()
                .filter(service -> service.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public ArrayList<Service> getAll() {
        return new ArrayList<>(services);
    }

    public boolean update(int id, Service updatedService) {
        Service service = get(id);
        if (service != null) {
            service.setName(updatedService.getName());
            service.setCategory(updatedService.getCategory());
            service.setType(updatedService.getType());
            service.setStatus(updatedService.getStatus());
            service.setDescription(updatedService.getDescription());
            service.setPrice(updatedService.getPrice());
            service.setDiscount(updatedService.getDiscount());
            service.setImages(updatedService.getImages());
            service.setVisibility(updatedService.isVisibility());
            service.setAvailability(updatedService.isAvailability());
            service.setSpecifity(updatedService.getSpecifity());
            service.setDuration(updatedService.getDuration());
            service.setMinEngagement(updatedService.getMinEngagement());
            service.setMaxEngagement(updatedService.getMaxEngagement());
            service.setReservationDeadline(updatedService.getReservationDeadline());
            service.setReservationMethod(updatedService.getReservationMethod());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        return services.removeIf(service -> service.getId() == id);
    }

    public Service getByName(String name) {
        return services.stream()
                .filter(service -> service.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<Service> search(String search) {
        String lowercasedSearch = search.toLowerCase();
        return services.stream()
                .filter(service -> service.getName().toLowerCase().contains(lowercasedSearch))
                .collect(Collectors.toList());
    }
}
