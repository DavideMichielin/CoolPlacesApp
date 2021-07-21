package utils;

public class Review {
    private String review;

    public Review() {
    }

    public Review(String review) {
        this.review = review;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    @Override
    public String toString() {
        return review;
    }
}
