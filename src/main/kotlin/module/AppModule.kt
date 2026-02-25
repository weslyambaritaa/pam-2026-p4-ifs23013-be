package org.delcom.module

import org.delcom.repositories.IPlantRepository
import org.delcom.repositories.PlantRepository
import org.delcom.repositories.IAnimalRepository // Tambahkan import ini
import org.delcom.repositories.AnimalRepository // Tambahkan import ini
import org.delcom.services.PlantService
import org.delcom.services.AnimalService // Tambahkan import ini
import org.delcom.services.ProfileService
import org.koin.dsl.module

val appModule = module {
    // Plant Repository & Service
    single<IPlantRepository> {
        PlantRepository()
    }
    single {
        PlantService(get())
    }

    // Animal Repository & Service (Tambahkan bagian ini)
    single<IAnimalRepository> {
        AnimalRepository()
    }
    single {
        AnimalService(get())
    }

    // Profile Service
    single {
        ProfileService()
    }
}