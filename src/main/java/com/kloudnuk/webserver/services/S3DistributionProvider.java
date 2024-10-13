package com.kloudnuk.webserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.kloudnuk.webserver.models.SoftwarePackage;
import com.kloudnuk.webserver.services.api.IDistributionProvider;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.List;
import java.util.Optional;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Service("s3Provider")
public class S3DistributionProvider implements IDistributionProvider {

        private static final Logger log = LoggerFactory.getLogger(S3DistributionProvider.class);
        private static final String BUCKET_NAME = "nuksoftware";

        private final Region region = Region.US_EAST_1;
        private final S3Client s3 = S3Client.builder().region(region).build();

        @Autowired
        private String PACKAGE_DIR;

        @Override
        public List<SoftwarePackage> listPackages() {
                try {
                        ListObjectsRequest request =
                                        ListObjectsRequest.builder().bucket(BUCKET_NAME).build();

                        ListObjectsResponse response = s3.listObjects(request);
                        List<S3Object> s3objects = response.contents();
                        List<SoftwarePackage> objects = s3objects.stream()
                                        .map(o -> new SoftwarePackage(o.key().split("_")[0], o.key()
                                                        .split("_")[1].split("\\.")[0].concat(".")
                                                        + o.key().split("_")[1].split("\\.")[1]
                                                                        .concat(".")
                                                        + o.key().split("_")[1].split("\\.")[2],
                                                        Date.from(o.lastModified()), BUCKET_NAME))
                                        .toList();
                        objects.stream().forEach(x -> log.debug(x.toString()));
                        return objects;
                } catch (S3Exception e) {
                        log.error("S3 error...", e);
                        return null;
                }
        }

        @Override
        public Optional<Resource> getPackageAsResource(String packageName) throws IOException {
                CompletableFuture<Optional<Resource>> future = CompletableFuture.supplyAsync(() -> {
                        Optional<Resource> resource;
                        try {
                                GetObjectRequest request = GetObjectRequest.builder()
                                                .key(packageName).bucket(BUCKET_NAME).build();
                                ResponseBytes<GetObjectResponse> responseBytes =
                                                s3.getObjectAsBytes(request);
                                byte[] data = responseBytes.asByteArray();

                                File packageFile = new File(PACKAGE_DIR.concat(packageName));
                                OutputStream os = new FileOutputStream(packageFile);
                                os.write(data);
                                os.close();
                                resource = Optional
                                                .ofNullable(new UrlResource(packageFile.toURI()));

                        } catch (S3Exception e) {
                                log.error("S3 error...", e.awsErrorDetails().errorMessage());
                                resource = Optional.empty();
                        } catch (FileNotFoundException fnfe) {
                                log.error("File not found...", fnfe);
                                resource = Optional.empty();
                        } catch (IOException ioe) {
                                log.error("IO output stream write error", ioe);
                                resource = Optional.empty();
                        }
                        return resource;
                });
                return future.join();
        }

        public void close() {
                this.s3.close();
        }
}
