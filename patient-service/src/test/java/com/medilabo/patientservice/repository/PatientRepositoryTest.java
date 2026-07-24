package com.medilabo.patientservice.repository;

import com.medilabo.patientservice.entity.Genre;
import com.medilabo.patientservice.entity.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration de {@link PatientRepository} sur une base H2 en mémoire.
 *
 * <p>Le schéma est ici généré par Hibernate depuis l'entité ({@code ddl-auto=create-drop})
 * plutôt que par {@code schema.sql} (dialecte MySQL) ou {@code data.sql} (désactivé),
 * afin d'isoler le test de la base de production.</p>
 */
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void save_shouldPersistPatientAndGenerateId() {
        Patient patient = new Patient("Test", "TestNone", LocalDate.of(1966, 12, 31),
                Genre.F, "1 Brookside St", "100-222-3333");

        Patient saved = patientRepository.save(patient);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findById_whenPatientExists_shouldReturnIt() {
        Patient saved = patientRepository.save(
                new Patient("Test", "TestBorderline", LocalDate.of(1945, 6, 24),
                        Genre.M, "2 High St", "200-333-4444"));

        Optional<Patient> found = patientRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getNom()).isEqualTo("TestBorderline");
    }

    @Test
    void findById_whenPatientMissing_shouldReturnEmpty() {
        Optional<Patient> found = patientRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    void save_withoutOptionalFields_shouldPersistNullAdresseAndTelephone() {
        Patient patient = new Patient("Test", "TestInDanger", LocalDate.of(2004, 6, 18),
                Genre.M, null, null);

        Patient saved = patientRepository.save(patient);
        Optional<Patient> found = patientRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAdresse()).isNull();
        assertThat(found.get().getTelephone()).isNull();
    }

    @Test
    void findAll_shouldReturnAllPersistedPatients() {
        patientRepository.save(new Patient("A", "A", LocalDate.of(1990, 1, 1), Genre.F, null, null));
        patientRepository.save(new Patient("B", "B", LocalDate.of(1991, 2, 2), Genre.M, null, null));

        assertThat(patientRepository.findAll()).hasSize(2);
    }
}
