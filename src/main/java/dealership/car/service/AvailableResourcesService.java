package dealership.car.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AvailableResourcesService {

    public final static Map<String, String> CAR_MODELS;

    public final static Map<String, String> CAR_ENGINES;

    public final static Map<String, String> CAR_BODY_TYPES;

    public final static Map<String, String> CAR_TRANSMISSION_TYPES;

    public final static Map<String, String> CAR_ADDITIONAL_EQUIPMENTS;

    static {
        CAR_MODELS = new HashMap<>();
        CAR_MODELS.put("Toyota Aygo", "Toyota Aygo");
        CAR_MODELS.put("Toyota Yaris", "Toyota Yaris");
        CAR_MODELS.put("Toyota Proace City", "Toyota Proace City");
        CAR_MODELS.put("Toyota Corolla", "Toyota Corolla");
        CAR_MODELS.put("Toyota C-HR", "Toyota C-HR");
        CAR_MODELS.put("Toyota RAV4", "Toyota RAV4");
        CAR_MODELS.put("Toyota Hilux", "Toyota Hilux");
        CAR_MODELS.put("Toyota Prius", "Toyota Prius");

        CAR_ENGINES = new HashMap<>();
        CAR_ENGINES.put("1,0 VVT-i 72 KM benzyna", "1,0 VVT-i 72 KM benzyna");
        CAR_ENGINES.put("1.5 VVT-i H 116 KM hybryda", "1.5 VVT-i H 116 KM hybryda");
        CAR_ENGINES.put("1.5 VVT-i 125 KM benzyna", "1.5 VVT-i 125 KM benzyna");
        CAR_ENGINES.put("1,8 Hybrid 122 KM", "1,8 Hybrid 122 KM");
        CAR_ENGINES.put("2.0 Dual VVT-iE 173 KM", "2.0 Dual VVT-iE 173 KM");
        CAR_ENGINES.put("2,0 Hybrid Dynamic Force 184 KM", "2,0 Hybrid Dynamic Force 184 KM");

        CAR_BODY_TYPES = new HashMap<>();
        CAR_BODY_TYPES.put("hatchback", "hatchback");
        CAR_BODY_TYPES.put("SUV", "SUV");
        CAR_BODY_TYPES.put("kombi", "kombi");
        CAR_BODY_TYPES.put("sedan", "sedan");

        CAR_TRANSMISSION_TYPES = new HashMap<>();
        CAR_TRANSMISSION_TYPES.put("5-stopniowa manualna (4X2)", "5-stopniowa manualna (4X2)");
        CAR_TRANSMISSION_TYPES.put("6-stopniowa manualna (4X2)", "6-stopniowa manualna (4X2)");
        CAR_TRANSMISSION_TYPES.put("Bezstopniowa automatyczna (4X2)", "Bezstopniowa automatyczna (4X2)");
        CAR_TRANSMISSION_TYPES.put("Direct Shift CVT (4X4)", "Direct Shift CVT (4X4)");
        CAR_TRANSMISSION_TYPES.put("6 M/T (4X4)", "6 M/T (4X4)");

        CAR_ADDITIONAL_EQUIPMENTS = new HashMap<>();
        CAR_ADDITIONAL_EQUIPMENTS.put("Nawigacja satelitarna","Nawigacja satelitarna");
        CAR_ADDITIONAL_EQUIPMENTS.put("Gwarancja serwisowa","Gwarancja serwisowa");
        CAR_ADDITIONAL_EQUIPMENTS.put("Komplet dywaników","Komplet dywaników");
        CAR_ADDITIONAL_EQUIPMENTS.put("Uchwyt na tablet","Uchwyt na tablet");
        CAR_ADDITIONAL_EQUIPMENTS.put("Podgrzewanie foteli przednich","Podgrzewanie foteli przednich");
        CAR_ADDITIONAL_EQUIPMENTS.put("Stopnie boczne","Stopnie boczne");
        CAR_ADDITIONAL_EQUIPMENTS.put("Bagażnik rowerowy","Bagażnik rowerowy");
        CAR_ADDITIONAL_EQUIPMENTS.put("Czujniki parkowania","Czujniki parkowania");
        CAR_ADDITIONAL_EQUIPMENTS.put("Kamera cofania","Kamera cofania");
    }

    public Map<String, String> getAvailableModels() {
        return CAR_MODELS;
    }

    public Map<String, String> getAvailableEngines() {
        return CAR_ENGINES;
    }

    public Map<String, String> getAvailableBodyTypes() {
        return CAR_BODY_TYPES;
    }

    public Map<String, String> getAvailableTransmissionTypes() {
        return CAR_TRANSMISSION_TYPES;
    }

    public Map<String, String> getAvailableAdditionalEquipments() {
        return CAR_ADDITIONAL_EQUIPMENTS;
    }
}
