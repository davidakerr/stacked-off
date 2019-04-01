package org.tools4j.stacked.index

import java.io.File

class SeDirParser(
    private val zipFileParser: SeZipFileParser,
    private val indexes: Indexes) {

    fun parseFromClasspath(pathOnClasspath: String, filter: (SeSite) -> Boolean) {
        val archiveFile = getFileOnClasspath(this.javaClass,pathOnClasspath)
        return parse(archiveFile.absolutePath, filter)
    }

    fun parse(dirPath: String, filter: (SeSite) -> Boolean) {
        var nextIndexedSiteId = indexes.indexedSiteIndex.getHighestIndexedSiteId() + 1
        val dir = File(dirPath).absolutePath
        val dirContents = SeDir(dir).getContents()
        val seDirSites = dirContents.getSites()
        for (seDirSite in seDirSites) {
            parseSeSite(seDirSite, filter, nextIndexedSiteId++.toString())
        }
    }

    private fun parseSeSite(
        seDirSite: SeDirSite,
        filter: (SeSite) -> Boolean,
        newIndexedSiteId: String
    ) {
        val seSite = seDirSite.site
        if (!filter(seSite)) return
        val matchingExistingIndexedSites = indexes.indexedSiteIndex.getMatching(seSite)
        val indexingSite = IndexingSiteImpl(
            newIndexedSiteId,
            "2019-10-11T10:00:00",
            seSite
        )
        try {
            for (zipFile in seDirSite.zipFiles) {
                zipFileParser.parse(newIndexedSiteId, zipFile.absolutePath)
            }
            println("Finished parsing site $seSite")
            indexes.purgeSites(matchingExistingIndexedSites)
            indexes.indexedSiteIndex.addItem(indexingSite.finished(true, null))

        } catch (e: Exception) {
            println(e.message)
            val exceptionAsString = if(e is ExtractorException) e.message else ExceptionToString(e).toString()
            if (matchingExistingIndexedSites.isNotEmpty()) {
                indexes.purgeSite(newIndexedSiteId)
            }
            indexes.indexedSiteIndex.addItem(indexingSite.finished(false, exceptionAsString))
        }
    }
}