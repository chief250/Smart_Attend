package com.smartattend.config;

import com.smartattend.model.entity.*;
import com.smartattend.model.enums.*;
import com.smartattend.repository.*;
import com.smartattend.service.QrTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final FaceReferenceRepository faceReferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final QrTokenService qrTokenService;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data creation.");
            return;
        }

        log.info("Seeding initial data for Smart Attendance System...");

        // 1. Create Admin
        User adminUser = User.builder()
                .fullName("System Administrator")
                .email("admin@university.edu")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(adminUser);

        // 2. Create Lecturer: Dr. Sarah Abdullah
        User lecturerUser = User.builder()
                .fullName("Dr. Sarah Abdullah")
                .email("sarah.lecturer@university.edu")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.LECTURER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(lecturerUser);

        Lecturer lecturer = Lecturer.builder()
                .user(lecturerUser)
                .staffNumber("L-2019-CS-0042")
                .department("Computer Science")
                .build();
        lecturerRepository.save(lecturer);

        // 3. Create Student: Ahmad Rizwan
        User studentUser = User.builder()
                .fullName("Ahmad Rizwan")
                .email("ahmad.student@university.edu")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.STUDENT)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(studentUser);

        Student student = Student.builder()
                .user(studentUser)
                .studentNumber("S2021-CS-4891")
                .program("Computer Science")
                .classGroup("Section A")
                .build();
        studentRepository.save(student);

        // Register default face reference
        FaceReference faceReference = FaceReference.builder()
                .student(student)
                .referenceData("DEFAULT_FACE_TEMPLATE_VERIFIED".getBytes())
                .build();
        faceReferenceRepository.save(faceReference);

        // 4. Create additional students
        String[][] additionalStudents = {
                {"Nur Faizah Hassan", "nur.faizah@university.edu", "S2021-CS-4782"},
                {"Muhammad Aiman", "aiman@university.edu", "S2021-CS-4803"},
                {"Siti Khadijah Mohd", "khadijah@university.edu", "S2021-CS-4819"},
                {"Zulaikha Azman", "zulaikha@university.edu", "S2021-CS-4856"},
                {"Jean Pierre", "jean.pierre@university.edu", "24RP00603"}
        };

        for (String[] sData : additionalStudents) {
            User u = User.builder()
                    .fullName(sData[0])
                    .email(sData[1])
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(UserRole.STUDENT)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(u);

            Student st = Student.builder()
                    .user(u)
                    .studentNumber(sData[2])
                    .program("Computer Science")
                    .classGroup("Section A")
                    .build();
            studentRepository.save(st);

            FaceReference fr = FaceReference.builder()
                    .student(st)
                    .referenceData("DEFAULT_FACE_TEMPLATE_VERIFIED".getBytes())
                    .build();
            faceReferenceRepository.save(fr);
        }

        // 5. Create Courses
        Course cs301 = Course.builder()
                .courseCode("CS301")
                .courseName("Data Structures & Algorithms")
                .description("Fundamental data structures, trees, graphs, sorting, and complexity")
                .lecturer(lecturer)
                .academicPeriod("Semester 1 • 2026")
                .status(UserStatus.ACTIVE)
                .build();
        courseRepository.save(cs301);

        Course mth201 = Course.builder()
                .courseCode("MTH201")
                .courseName("Calculus II")
                .description("Multivariable calculus and differential equations")
                .lecturer(lecturer)
                .academicPeriod("Semester 1 • 2026")
                .status(UserStatus.ACTIVE)
                .build();
        courseRepository.save(mth201);

        Course eng102 = Course.builder()
                .courseCode("ENG102")
                .courseName("Technical Writing")
                .description("Engineering communication and technical report writing")
                .lecturer(lecturer)
                .academicPeriod("Semester 1 • 2026")
                .status(UserStatus.ACTIVE)
                .build();
        courseRepository.save(eng102);

        Course phy101 = Course.builder()
                .courseCode("PHY101")
                .courseName("Physics Fundamentals")
                .description("Mechanics, waves, and thermodynamics")
                .lecturer(lecturer)
                .academicPeriod("Semester 1 • 2026")
                .status(UserStatus.ACTIVE)
                .build();
        courseRepository.save(phy101);

        // 6. Enroll Students in Courses
        for (Student st : studentRepository.findAll()) {
            enrollmentRepository.save(Enrollment.builder().student(st).course(cs301).academicPeriod("Semester 1 • 2026").build());
            enrollmentRepository.save(Enrollment.builder().student(st).course(mth201).academicPeriod("Semester 1 • 2026").build());
            enrollmentRepository.save(Enrollment.builder().student(st).course(eng102).academicPeriod("Semester 1 • 2026").build());
            enrollmentRepository.save(Enrollment.builder().student(st).course(phy101).academicPeriod("Semester 1 • 2026").build());
        }

        // 7. Create Past Sessions & Past Attendance for demo stats
        for (int i = 5; i >= 1; i--) {
            AttendanceSession pastSession = AttendanceSession.builder()
                    .course(cs301)
                    .lecturer(lecturer)
                    .sessionDate(LocalDate.now().minusDays(i * 2))
                    .startTime(LocalDateTime.now().minusDays(i * 2).withHour(9).withMinute(0))
                    .endTime(LocalDateTime.now().minusDays(i * 2).withHour(10).withMinute(30))
                    .lateThresholdMinutes(15)
                    .status(SessionStatus.CLOSED)
                    .build();
            sessionRepository.save(pastSession);

            // Record attendance for Ahmad
            attendanceRecordRepository.save(AttendanceRecord.builder()
                    .student(student)
                    .session(pastSession)
                    .checkInTime(pastSession.getStartTime().plusMinutes(5))
                    .status(AttendanceStatus.PRESENT)
                    .verificationStatus(VerificationStatus.VERIFIED)
                    .checkInMethod("QR_FACE")
                    .build());
        }

        // 8. Create Today's Live Active Session with QR Token
        AttendanceSession liveSession = AttendanceSession.builder()
                .course(cs301)
                .lecturer(lecturer)
                .sessionDate(LocalDate.now())
                .startTime(LocalDateTime.now().withHour(9).withMinute(0))
                .endTime(LocalDateTime.now().plusHours(2))
                .lateThresholdMinutes(15)
                .status(SessionStatus.OPEN)
                .build();
        sessionRepository.save(liveSession);

        // Generate QR code token for today's live session
        qrTokenService.generateToken(liveSession.getId());

        log.info("Initial data successfully seeded!");
        log.info("Admin: admin@university.edu / admin123");
        log.info("Lecturer: sarah.lecturer@university.edu / password123");
        log.info("Student: ahmad.student@university.edu / password123");
    }
}
