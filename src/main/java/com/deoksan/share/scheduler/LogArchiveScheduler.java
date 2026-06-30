package com.deoksan.share.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

/**
 * 로그 파일 주간 아카이브 스케줄러.
 *
 * <h3>동작</h3>
 * 매주 일요일 새벽 3시(KST)에 지난 주 로그 파일(*.gz)을 Cloudflare R2에 업로드한다.
 * 업로드된 파일은 R2 버킷에 {@code logs/{year}/W{week}/{filename}} 형태로 저장된다.
 *
 * <h3>활성화 조건</h3>
 * {@code app.log-archive.enabled=true} 설정 시에만 빈이 생성된다.
 * 로컬/개발 환경에서는 비활성화 상태(기본값 false)로 실행된다.
 *
 * <h3>Cloudflare R2 설정</h3>
 * R2는 S3 호환 API를 제공하므로 AWS SDK v2를 그대로 사용한다.
 * 엔드포인트만 R2 URL로 교체하면 된다.
 * <pre>
 * app.log-archive.s3-endpoint=https://{account-id}.r2.cloudflarestorage.com
 * app.log-archive.s3-bucket=personal-blog-logs
 * app.log-archive.access-key=${R2_ACCESS_KEY}
 * app.log-archive.secret-key=${R2_SECRET_KEY}
 * </pre>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.log-archive.enabled", havingValue = "true")
public class LogArchiveScheduler {

    private static final DateTimeFormatter WEEK_FMT = DateTimeFormatter.ofPattern("'W'ww");
    private static final DateTimeFormatter YEAR_FMT = DateTimeFormatter.ofPattern("yyyy");

    @Value("${logging.file.path:logs}")
    private String logPath;

    @Value("${app.log-archive.s3-endpoint}")
    private String s3Endpoint;

    @Value("${app.log-archive.s3-bucket}")
    private String s3Bucket;

    @Value("${app.log-archive.access-key}")
    private String accessKey;

    @Value("${app.log-archive.secret-key}")
    private String secretKey;

    /** 매주 일요일 03:00 KST (UTC 18:00 토요일) */
    @Scheduled(cron = "${app.scheduler.log-archive-cron:0 0 18 * * SAT}", zone = "UTC")
    public void archiveLogs() {
        log.info("[LogArchive] 주간 로그 아카이브 시작 logPath={}", logPath);

        S3Client s3 = buildS3Client();
        LocalDate today = LocalDate.now();
        String prefix = YEAR_FMT.format(today) + "/" + WEEK_FMT.format(today) + "/";

        int uploaded = 0;
        int failed = 0;

        try (Stream<Path> files = Files.list(Path.of(logPath))) {
            Iterable<Path> gzFiles = files
                    .filter(p -> p.getFileName().toString().endsWith(".gz"))
                    ::iterator;

            for (Path file : gzFiles) {
                try {
                    String key = "logs/" + prefix + file.getFileName();
                    PutObjectRequest req = PutObjectRequest.builder()
                            .bucket(s3Bucket)
                            .key(key)
                            .contentType("application/gzip")
                            .build();
                    s3.putObject(req, RequestBody.fromFile(file));
                    log.info("[LogArchive] 업로드 완료: {}", key);
                    uploaded++;
                } catch (Exception e) {
                    log.error("[LogArchive] 업로드 실패 file={}: {}", file.getFileName(), e.getMessage());
                    failed++;
                }
            }
        } catch (IOException e) {
            log.error("[LogArchive] 로그 디렉토리 접근 실패: {}", e.getMessage());
            return;
        } finally {
            s3.close();
        }

        log.info("[LogArchive] 완료 — 성공={} 실패={}", uploaded, failed);
    }

    private S3Client buildS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(s3Endpoint))
                .region(Region.of("auto"))     // R2는 region이 "auto"
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
