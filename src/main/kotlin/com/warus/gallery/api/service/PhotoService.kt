package com.warus.gallery.api.service

import com.warus.gallery.api.config.UploadProperties
import com.warus.gallery.api.db.model.Photo
import com.warus.gallery.api.db.repository.PhotoRepository
import com.warus.gallery.api.model.PhotoUpdateRequest
import com.warus.gallery.api.model.PhotoUploadRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class PhotoService(
   private val photoRepository: PhotoRepository,
   private val uploadProperties: UploadProperties
) {

   fun save(file: MultipartFile, metadata: PhotoUploadRequest): Photo {
      val extension = file.originalFilename?.substringAfterLast('.', "")!!
      val fileName = UUID.randomUUID().toString() + "." + extension
      val uploadDir = Paths.get(uploadProperties.path)

      if (!Files.exists(uploadDir)) {
         Files.createDirectories(uploadDir)
      }

      val filePath = uploadDir.resolve(fileName)
      file.inputStream.use { input ->
         Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING)
      }

      val imageUrl = "/images/$fileName"

      val photo = Photo(
         fileName = fileName,
         imageUrl = imageUrl,
         height = metadata.height,
         material = metadata.material,
         color = metadata.color,
         type = metadata.type
      )

      return photoRepository.save(photo)
   }

   fun getPhotosWithFilters(
      colors: List<String>?,
      types: List<String>?,
      materials: List<String>?,
      minHeight: Int?,
      maxHeight: Int?,
      page: Int,
      size: Int
   ): Page<Photo> {
      val spec = Specification.where<Photo>(null)
         .and(colors?.let { PhotoSpecs.hasAnyColor(it) })
         .and(types?.let { PhotoSpecs.hasAnyType(it) })
         .and(materials?.let { PhotoSpecs.hasAnyMaterial(it) })
         .and(minHeight?.let { PhotoSpecs.hasMinHeight(it) })
         .and(maxHeight?.let { PhotoSpecs.hasMaxHeight(it) })

      return photoRepository.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()))
   }

   fun getPhotoById(id: Long): Photo =
      photoRepository.findById(id)
         .orElseThrow { NoSuchElementException("Photo not found with id $id") }

   fun updatePhoto(id: Long, request: PhotoUpdateRequest): Photo {
      val photo = photoRepository.findById(id)
         .orElseThrow { NoSuchElementException("Photo not found with id $id") }

      val updated = photo.copy(
         height = request.height,
         material = request.material,
         color = request.color,
         type = request.type
      )

      return photoRepository.save(updated)
   }

   fun deletePhoto(id: Long) {
      val photo = photoRepository.findById(id)
         .orElseThrow { NoSuchElementException("Photo not found with id $id") }

      val path: Path = Paths.get(uploadProperties.path).resolve(photo.fileName)
      Files.deleteIfExists(path)

      photoRepository.delete(photo)
   }

}
