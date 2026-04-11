package com.bupt.ta.recruitment.service;

import com.bupt.ta.recruitment.model.Application;
import com.bupt.ta.recruitment.model.Job;
import com.bupt.ta.recruitment.model.TAProfile;
import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Module Organiser (MO) operations.
 * Handles the business logic for Task 5, 6, and 7.
 */
public class MOService {
    private final CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
    private final CsvStorage<Application> applicationStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);
    private final CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
    private final CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);

    public List<Job> getJobsByMo(String moId) {
        return jobStorage.loadAll().stream()
                .filter(job -> moId.equals(job.getMoId()))
                .collect(Collectors.toList());
    }

    public void postJob(Job job) throws Exception {
        List<Job> jobs = jobStorage.loadAll();
        jobs.add(job);
        jobStorage.saveAll(jobs);
    }

    public void closeJob(String jobId) throws Exception {
        List<Job> jobs = jobStorage.loadAll();
        for (Job job : jobs) {
            if (job.getId().equals(jobId)) {
                // 使用 Job.JobStatus 枚举
                job.setStatus(Job.JobStatus.CLOSED);
                break;
            }
        }
        jobStorage.saveAll(jobs);
    }

    public List<Application> getApplicationsByMoJob(String jobId) {
        return applicationStorage.loadAll().stream()
                .filter(app -> app.getJobId().equals(jobId))
                .collect(Collectors.toList());
    }

    public User getUserById(String userId) {
        return userStorage.findById(userId, User::getId);
    }

    public TAProfile getProfileByUserId(String userId) {
        return profileStorage.loadAll().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst().orElse(null);
    }

    public void updateApplicationStatus(String appId, String status) throws Exception {
        List<Application> apps = applicationStorage.loadAll();
        for (Application app : apps) {
            if (app.getId().equals(appId)) {
                // 将字符串转换为 Application.AppStatus 枚举
                app.setStatus(Application.AppStatus.valueOf(status));
                break;
            }
        }
        applicationStorage.saveAll(apps);
    }
}
