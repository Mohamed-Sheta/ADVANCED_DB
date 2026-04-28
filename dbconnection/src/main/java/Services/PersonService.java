package Services;

import DAO.PersonDAO;
import odb.Person;

public class PersonService {
    private final PersonDAO personDAO = new PersonDAO();

    public Person register(String name, String email, String password, String ssn, String phone, String address, String gender) {
        String trimmedName = requireNonBlank(name, "Name is required");
        String trimmedEmail = requireValidEmail(email);
        String trimmedPassword = requireNonBlank(password, "Password is required");
        String trimmedSsn = requireValidSsn(ssn);
        String trimmedPhone = requireValidPhone(phone);
        String trimmedAddress = requireNonBlank(address, "Address is required");
        String normalizedGender = requireValidGender(gender);

        Person existing = personDAO.findByEmail(email.trim());
        if (existing != null) {
            throw new IllegalStateException("Email already registered");
        }
        Person person = new Person(
                trimmedName,
                trimmedEmail,
                trimmedPassword,
                trimmedSsn,
                trimmedPhone,
                trimmedAddress,
                normalizedGender
        );
        personDAO.insert(person);
        return person;
    }

    public Person login(String email, String password) {
        String trimmedEmail = requireValidEmail(email);
        String trimmedPassword = requireNonBlank(password, "Password is required");
        return personDAO.authenticate(trimmedEmail, trimmedPassword);
    }

    public Person updateProfile(Person person, String name, String email, String ssn, String phone, String address, String gender) {
        if (person == null) {
            throw new IllegalArgumentException("Person is required");
        }
        String trimmedName = requireNonBlank(name, "Name is required");
        String trimmedEmail = requireValidEmail(email);
        String trimmedSsn = requireValidSsn(ssn);
        String trimmedPhone = requireValidPhone(phone);
        String trimmedAddress = requireNonBlank(address, "Address is required");
        String normalizedGender = requireValidGender(gender);

        Person existing = personDAO.findByEmail(email.trim());
        if (existing != null && !existing.getId().equals(person.getId())) {
            throw new IllegalStateException("Email already registered");
        }

        person.setName(trimmedName);
        person.setEmail(trimmedEmail);
        person.setSsn(trimmedSsn);
        person.setPhone(trimmedPhone);
        person.setAddress(trimmedAddress);
        person.setGender(normalizedGender);
        personDAO.update(person);
        return person;
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String requireValidEmail(String email) {
        String trimmed = requireNonBlank(email, "Email is required");
        int atIndex = trimmed.indexOf('@');
        int dotIndex = trimmed.lastIndexOf('.');
        if (atIndex <= 0 || dotIndex <= atIndex + 1 || dotIndex >= trimmed.length() - 1) {
            throw new IllegalArgumentException("Email format is invalid");
        }
        return trimmed;
    }

    private String requireValidSsn(String ssn) {
        String trimmed = requireNonBlank(ssn, "SSN is required");
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        if (digitsOnly.length() != 9) {
            throw new IllegalArgumentException("SSN must contain 9 digits");
        }
        return trimmed;
    }

    private String requireValidPhone(String phone) {
        String trimmed = requireNonBlank(phone, "Phone is required");
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 7) {
            throw new IllegalArgumentException("Phone number is invalid");
        }
        return trimmed;
    }

    private String requireValidGender(String gender) {
        String trimmed = requireNonBlank(gender, "Gender is required");
        if (trimmed.equalsIgnoreCase("male")) {
            return "Male";
        }
        if (trimmed.equalsIgnoreCase("female")) {
            return "Female";
        }
        throw new IllegalArgumentException("Gender must be Male or Female");
    }
}
