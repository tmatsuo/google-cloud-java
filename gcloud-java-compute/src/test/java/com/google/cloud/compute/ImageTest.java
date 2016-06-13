/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.compute;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.util.List;

public class ImageTest {

  private static final ImageId IMAGE_ID = ImageId.of("project", "image");
  private static final String GENERATED_ID = "42";
  private static final Long CREATION_TIMESTAMP = 1453293540000L;
  private static final String DESCRIPTION = "description";
  private static final ImageInfo.Status STATUS = ImageInfo.Status.READY;
  private static final List<LicenseId> LICENSES = ImmutableList.of(
      LicenseId.of("project", "license1"), LicenseId.of("project", "license2"));
  private static final Long DISK_SIZE_GB = 42L;
  private static final String STORAGE_SOURCE = "source";
  private static final Long ARCHIVE_SIZE_BYTES = 24L;
  private static final String SHA1_CHECKSUM = "checksum";
  private static final DiskId SOURCE_DISK =  DiskId.of("project", "zone", "disk");
  private static final String SOURCE_DISK_ID = "diskId";
  private static final ImageConfiguration.SourceType SOURCE_TYPE = ImageConfiguration.SourceType.RAW;
  private static final StorageImageConfiguration STORAGE_CONFIGURATION =
      StorageImageConfiguration.builder(STORAGE_SOURCE)
          .archiveSizeBytes(ARCHIVE_SIZE_BYTES)
          .containerType(StorageImageConfiguration.ContainerType.TAR)
          .sha1(SHA1_CHECKSUM)
          .sourceType(SOURCE_TYPE)
          .build();
  private static final DiskImageConfiguration DISK_CONFIGURATION =
      DiskImageConfiguration.builder(SOURCE_DISK)
          .archiveSizeBytes(ARCHIVE_SIZE_BYTES)
          .sourceDiskId(SOURCE_DISK_ID)
          .sourceType(SOURCE_TYPE)
          .build();
  private static final DeprecationStatus<ImageId> DEPRECATION_STATUS =
      DeprecationStatus.of(DeprecationStatus.Status.DELETED, IMAGE_ID);

  private final Compute serviceMockReturnsOptions = createStrictMock(Compute.class);
  private final ComputeOptions mockOptions = createMock(ComputeOptions.class);
  private Compute compute;
  private Image image;
  private Image diskImage;
  private Image storageImage;

  private void initializeExpectedImage(int optionsCalls) {
    expect(serviceMockReturnsOptions.options()).andReturn(mockOptions).times(optionsCalls);
    replay(serviceMockReturnsOptions);
    diskImage = new Image.Builder(serviceMockReturnsOptions, IMAGE_ID, DISK_CONFIGURATION)
        .generatedId(GENERATED_ID)
        .creationTimestamp(CREATION_TIMESTAMP)
        .description(DESCRIPTION)
        .status(STATUS)
        .diskSizeGb(DISK_SIZE_GB)
        .licenses(LICENSES)
        .deprecationStatus(DEPRECATION_STATUS)
        .build();
    storageImage = new Image.Builder(serviceMockReturnsOptions, IMAGE_ID, STORAGE_CONFIGURATION)
        .generatedId(GENERATED_ID)
        .creationTimestamp(CREATION_TIMESTAMP)
        .description(DESCRIPTION)
        .status(STATUS)
        .diskSizeGb(DISK_SIZE_GB)
        .licenses(LICENSES)
        .deprecationStatus(DEPRECATION_STATUS)
        .build();
    compute = createStrictMock(Compute.class);
  }

  private void initializeImage() {
    image = new Image.Builder(compute, IMAGE_ID, DISK_CONFIGURATION)
        .generatedId(GENERATED_ID)
        .creationTimestamp(CREATION_TIMESTAMP)
        .description(DESCRIPTION)
        .status(STATUS)
        .diskSizeGb(DISK_SIZE_GB)
        .licenses(LICENSES)
        .deprecationStatus(DEPRECATION_STATUS)
        .build();
  }

  @Test
  public void testToBuilder() {
    initializeExpectedImage(12);
    compareImage(diskImage, diskImage.toBuilder().build());
    compareImage(storageImage, storageImage.toBuilder().build());
    Image newImage = diskImage.toBuilder().description("newDescription").build();
    assertEquals("newDescription", newImage.description());
    newImage = newImage.toBuilder().description("description").build();
    compareImage(diskImage, newImage);
  }

  @Test
  public void testToBuilderIncomplete() {
    initializeExpectedImage(6);
    ImageInfo imageInfo = ImageInfo.of(IMAGE_ID, DISK_CONFIGURATION);
    Image image =
        new Image(serviceMockReturnsOptions, new ImageInfo.BuilderImpl(imageInfo));
    compareImage(image, image.toBuilder().build());
  }

  @Test
  public void testBuilder() {
    initializeExpectedImage(3);
    assertEquals(GENERATED_ID, diskImage.generatedId());
    assertEquals(IMAGE_ID, diskImage.imageId());
    assertEquals(CREATION_TIMESTAMP, diskImage.creationTimestamp());
    assertEquals(DESCRIPTION, diskImage.description());
    assertEquals(DISK_CONFIGURATION, diskImage.configuration());
    assertEquals(STATUS, diskImage.status());
    assertEquals(DISK_SIZE_GB, diskImage.diskSizeGb());
    assertEquals(LICENSES, diskImage.licenses());
    assertEquals(DEPRECATION_STATUS, diskImage.deprecationStatus());
    assertSame(serviceMockReturnsOptions, diskImage.compute());
    assertEquals(GENERATED_ID, storageImage.generatedId());
    assertEquals(IMAGE_ID, storageImage.imageId());
    assertEquals(CREATION_TIMESTAMP, storageImage.creationTimestamp());
    assertEquals(DESCRIPTION, storageImage.description());
    assertEquals(STORAGE_CONFIGURATION, storageImage.configuration());
    assertEquals(STATUS, storageImage.status());
    assertEquals(DISK_SIZE_GB, storageImage.diskSizeGb());
    assertEquals(LICENSES, storageImage.licenses());
    assertEquals(DEPRECATION_STATUS, storageImage.deprecationStatus());
    assertSame(serviceMockReturnsOptions, storageImage.compute());
    ImageId imageId = ImageId.of("otherImage");
    Image image = new Image.Builder(serviceMockReturnsOptions, IMAGE_ID, STORAGE_CONFIGURATION)
        .imageId(imageId)
        .configuration(DISK_CONFIGURATION)
        .build();
    assertNull(image.generatedId());
    assertEquals(imageId, image.imageId());
    assertNull(image.creationTimestamp());
    assertNull(image.description());
    assertEquals(DISK_CONFIGURATION, image.configuration());
    assertNull(image.status());
    assertNull(image.diskSizeGb());
    assertNull(image.licenses());
    assertNull(image.deprecationStatus());
    assertSame(serviceMockReturnsOptions, image.compute());
  }

  @Test
  public void testToAndFromPb() {
    initializeExpectedImage(12);
    compareImage(diskImage,
        Image.fromPb(serviceMockReturnsOptions, diskImage.toPb()));
    compareImage(storageImage,
        Image.fromPb(serviceMockReturnsOptions, storageImage.toPb()));
    Image image =
        new Image.Builder(serviceMockReturnsOptions, IMAGE_ID, DISK_CONFIGURATION).build();
    compareImage(image, Image.fromPb(serviceMockReturnsOptions, image.toPb()));
  }

  @Test
  public void testDeleteOperation() {
    initializeExpectedImage(3);
    expect(compute.options()).andReturn(mockOptions);
    Operation operation = new Operation.Builder(serviceMockReturnsOptions)
        .operationId(GlobalOperationId.of("project", "op"))
        .build();
    expect(compute.deleteImage(IMAGE_ID)).andReturn(operation);
    replay(compute);
    initializeImage();
    assertSame(operation, image.delete());
  }

  @Test
  public void testDeleteNull() {
    initializeExpectedImage(2);
    expect(compute.options()).andReturn(mockOptions);
    expect(compute.deleteImage(IMAGE_ID)).andReturn(null);
    replay(compute);
    initializeImage();
    assertNull(image.delete());
  }

  @Test
  public void testExists_True() throws Exception {
    initializeExpectedImage(2);
    Compute.ImageOption[] expectedOptions = {Compute.ImageOption.fields()};
    expect(compute.options()).andReturn(mockOptions);
    expect(compute.getImage(IMAGE_ID, expectedOptions)).andReturn(diskImage);
    replay(compute);
    initializeImage();
    assertTrue(image.exists());
    verify(compute);
  }

  @Test
  public void testExists_False() throws Exception {
    initializeExpectedImage(2);
    Compute.ImageOption[] expectedOptions = {Compute.ImageOption.fields()};
    expect(compute.options()).andReturn(mockOptions);
    expect(compute.getImage(IMAGE_ID, expectedOptions)).andReturn(null);
    replay(compute);
    initializeImage();
    assertFalse(image.exists());
    verify(compute);
  }

  @Test
  public void testReload() throws Exception {
    initializeExpectedImage(5);
    expect(compute.options()).andReturn(mockOptions);
    expect(compute.getImage(IMAGE_ID)).andReturn(storageImage);
    replay(compute);
    initializeImage();
    Image updateImage = image.reload();
    compareImage(storageImage, updateImage);
    verify(compute);
  }

  @Test
  public void testReloadNull() throws Exception {
    initializeExpectedImage(2);
    expect(compute.options()).andReturn(mockOptions);
    expect(compute.getImage(IMAGE_ID)).andReturn(null);
    replay(compute);
    initializeImage();
    assertNull(image.reload());
    verify(compute);
  }

  @Test
  public void testReloadWithOptions() throws Exception {
    initializeExpectedImage(5);
    expect(compute.options()).andReturn(mockOptions);
    expect(compute.getImage(IMAGE_ID, Compute.ImageOption.fields())).andReturn(storageImage);
    replay(compute);
    initializeImage();
    Image updateImage = image.reload(Compute.ImageOption.fields());
    compareImage(storageImage, updateImage);
    verify(compute);
  }

  @Test
  public void testDeprecateImage() {
    initializeExpectedImage(3);
    expect(compute.options()).andReturn(mockOptions);
    Operation operation = new Operation.Builder(serviceMockReturnsOptions)
        .operationId(GlobalOperationId.of("project", "op"))
        .build();
    DeprecationStatus<ImageId> status = DeprecationStatus.of(DeprecationStatus.Status.DEPRECATED, IMAGE_ID);
    expect(compute.deprecate(IMAGE_ID, status)).andReturn(operation);
    replay(compute);
    initializeImage();
    assertSame(operation, image.deprecate(status));
  }

  @Test
  public void testDeprecateNull() {
    initializeExpectedImage(2);
    expect(compute.options()).andReturn(mockOptions);
    DeprecationStatus<ImageId> status = DeprecationStatus.of(DeprecationStatus.Status.DEPRECATED, IMAGE_ID);
    expect(compute.deprecate(IMAGE_ID, status)).andReturn(null);
    replay(compute);
    initializeImage();
    assertNull(image.deprecate(status));
  }

  public void compareImage(Image expected, Image value) {
    assertEquals(expected, value);
    assertEquals(expected.compute().options(), value.compute().options());
    assertEquals(expected.generatedId(), value.generatedId());
    assertEquals(expected.imageId(), value.imageId());
    assertEquals(expected.creationTimestamp(), value.creationTimestamp());
    assertEquals(expected.description(), value.description());
    assertEquals(expected.configuration(), value.configuration());
    assertEquals(expected.status(), value.status());
    assertEquals(expected.diskSizeGb(), value.diskSizeGb());
    assertEquals(expected.licenses(), value.licenses());
    assertEquals(expected.deprecationStatus(), value.deprecationStatus());
    assertEquals(expected.hashCode(), value.hashCode());
  }
}
