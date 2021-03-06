package org.tools4j.stacked.index

import org.apache.lucene.document.Document


class StagingCommentIndex(indexFactory: IndexFactory)
    : TypedIndex<StagingComment>(indexFactory, "comments") {

    override fun getIndexedFieldsAndRankings(): MutableMap<String, Float> {
        return mutableMapOf("text" to 10.0f)
    }

    override fun convertDocumentToItem(doc: Document): StagingComment = StagingComment(doc)

    override fun convertItemToDocument(comment: StagingComment): Document = comment.convertToDocument()

    fun getByPostId(postUid: String): List<StagingComment> {
        return searchByTerm("postId", postUid, UnscoredCollector())
    }
}