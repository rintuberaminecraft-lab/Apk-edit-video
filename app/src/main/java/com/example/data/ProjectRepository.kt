package com.example.data

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.ProjectEntity
import com.example.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProjectRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val projectDao = database.projectDao()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(EditorProject::class.java)

    fun getAllProjects(): Flow<List<EditorProject>> {
        return projectDao.getAllProjects().map { entities ->
            if (entities.isEmpty()) {
                val initial = DefaultSampleProvider.createInitialSampleProject()
                saveProject(initial)
                listOf(initial)
            } else {
                entities.mapNotNull { entity ->
                    try {
                        adapter.fromJson(entity.serializedJson)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }

    suspend fun getProjectById(id: String): EditorProject? = withContext(Dispatchers.IO) {
        val entity = projectDao.getProjectById(id) ?: return@withContext null
        try {
            adapter.fromJson(entity.serializedJson)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveProject(project: EditorProject) = withContext(Dispatchers.IO) {
        val json = adapter.toJson(project)
        val entity = ProjectEntity(
            id = project.id,
            name = project.name,
            aspectRatio = project.aspectRatio.name,
            durationMs = project.durationMs,
            updatedAt = System.currentTimeMillis(),
            createdAt = project.createdAt,
            thumbnailUri = project.thumbnailUri,
            serializedJson = json
        )
        projectDao.insertProject(entity)
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        projectDao.deleteProject(id)
    }
}
