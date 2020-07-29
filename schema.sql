-- Login information for a user
CREATE TABLE IF NOT EXISTS users
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(32) UNIQUE NOT NULL,
    password VARCHAR(60)        NOT NULL,
    type     CHAR               NOT NULL

);

-- Student linked to a user
CREATE TABLE IF NOT EXISTS students
(
    id         INT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE
);

-- Teacher linked to a user
CREATE TABLE IF NOT EXISTS teachers
(
    id         INT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE
);

-- Course taught by one teacher
CREATE TABLE IF NOT EXISTS courses
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    room        VARCHAR(10),
    teacher_id  INT,
    FOREIGN KEY (teacher_id) REFERENCES teachers (id)
);

-- Class period during the day
CREATE TABLE IF NOT EXISTS periods
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    display    VARCHAR(100) NOT NULL,
    start_time TIME         NOT NULL
);

-- Course enrollments by students
CREATE TABLE IF NOT EXISTS enrollments
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id  INT NOT NULL,
    period_id  INT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students (id),
    FOREIGN KEY (course_id) REFERENCES courses (id),
    FOREIGN KEY (period_id) REFERENCES periods (id),
    UNIQUE(student_id, period_id)
);

-- Assignment details
CREATE TABLE IF NOT EXISTS assignments
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255),
    total_points INT,
    due_date     DATE,
    course_id    INT NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses (id)
);

-- Grade given to a student for a specific assignment
CREATE TABLE IF NOT EXISTS grades
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    assignment_id INT NOT NULL,
    points        INT,
    graded_at     TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE,
    FOREIGN KEY (assignment_id) REFERENCES assignments (id) ON DELETE CASCADE,
    UNIQUE (student_id, assignment_id)
);
