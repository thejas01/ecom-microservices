package com.ecommerce.common.utils.response;

public class PageInfo {
    
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;

    // Constructors
    public PageInfo() {}

    public PageInfo(int currentPage, int pageSize, long totalElements, int totalPages, 
                   boolean first, boolean last, boolean hasNext, boolean hasPrevious) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    // Getters
    public int getCurrentPage() { return currentPage; }
    public int getPageSize() { return pageSize; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
    public boolean isHasNext() { return hasNext; }
    public boolean isHasPrevious() { return hasPrevious; }

    // Setters
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public void setFirst(boolean first) { this.first = first; }
    public void setLast(boolean last) { this.last = last; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
    public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;

        public Builder currentPage(int currentPage) { this.currentPage = currentPage; return this; }
        public Builder pageSize(int pageSize) { this.pageSize = pageSize; return this; }
        public Builder totalElements(long totalElements) { this.totalElements = totalElements; return this; }
        public Builder totalPages(int totalPages) { this.totalPages = totalPages; return this; }
        public Builder first(boolean first) { this.first = first; return this; }
        public Builder last(boolean last) { this.last = last; return this; }
        public Builder hasNext(boolean hasNext) { this.hasNext = hasNext; return this; }
        public Builder hasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; return this; }

        public PageInfo build() {
            return new PageInfo(currentPage, pageSize, totalElements, totalPages, 
                              first, last, hasNext, hasPrevious);
        }
    }
}