import { StorageFactory } from "./StorageFactory"

export class LocalStorageFactory implements StorageFactory {

    criarStorage(): Storage {
        return localStorage
    }
}