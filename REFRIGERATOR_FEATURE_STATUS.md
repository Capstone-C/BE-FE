# Refrigerator Feature Implementation Status

**Parent Issue**: [#44 - 냉장고 관리 기능](https://github.com/Capstone-C/BE-FE/issues/44)  
**Branch**: `feat/refrigerator`  
**PR**: [#145 - WIP Draft PR](https://github.com/Capstone-C/BE-FE/pull/145)  

---

## 📊 Overall Progress

**Completed**: 5/8 features (62.5%)  
**Remaining**: 3/8 features (37.5%)

| Feature ID | Sub-Issue | Status | API Endpoint |
|------------|-----------|--------|--------------|
| REF-01 | #45 내 냉장고 식재료 목록 조회 | ✅ **COMPLETE** | `GET /api/v1/refrigerator/items` |
| REF-02 | #46 수동으로 식재료 추가 | ✅ **COMPLETE** | `POST /api/v1/refrigerator/items` |
| REF-03 | #47 영수증 스캔으로 식재료 추가 | ✅ **COMPLETE** | `POST /api/v1/refrigerator/scan/receipt` |
| REF-04 | #48 구매 내역으로 식재료 추가 | ❌ **NOT IMPLEMENTED** | - |
| REF-05 | #49 식재료 정보 수정 | ✅ **COMPLETE** | `PUT /api/v1/refrigerator/items/{id}` |
| REF-06 | #50 식재료 삭제 | ✅ **COMPLETE** | `DELETE /api/v1/refrigerator/items/{id}` |
| REF-07 | #51 보유 재료 기반 레시피 추천 | ❌ **NOT IMPLEMENTED** | - |
| REF-08 | #73 레시피 사용 재료 자동 차감 | ❌ **NOT IMPLEMENTED** | - |

---

## ✅ Completed Features

### REF-01: 내 냉장고 식재료 목록 조회 (#45)

**API**: `GET /api/v1/refrigerator/items?sortBy={expirationDate|name|createdAt}`

**Features**:
- Sort by expiration date (default), name, or creation date
- Returns list of refrigerator items with calculated D-day
- Automatic duplicate prevention (unique per member + name)

**Response Example**:
```json
{
  "items": [
    {
      "id": 1,
      "name": "우유",
      "quantity": 2,
      "unit": "개",
      "expirationDate": "2024-05-15",
      "memo": "저지방",
      "createdAt": "2024-05-01T10:00:00",
      "daysUntilExpiration": 3,
      "expirationSoon": true,
      "expired": false
    }
  ]
}
```

---

### REF-02: 수동으로 식재료 추가 (#46)

**API**: `POST /api/v1/refrigerator/items`

**Features**:
- Single item registration
- Duplicate check (throws exception if exists)
- Optional fields: expirationDate, memo, unit
- Default quantity: 1

**Request Example**:
```json
{
  "name": "양파",
  "quantity": 3,
  "unit": "개",
  "expirationDate": "2024-06-01",
  "memo": "카레용"
}
```

**Bulk Add**: `POST /api/v1/refrigerator/items/bulk`
- Skips duplicates (no exception)
- Returns success/fail counts

---

### REF-03: 영수증 스캔으로 식재료 추가 (#47) ✨ **NEW**

**API**: `POST /api/v1/refrigerator/scan/receipt`

**Implementation**:
- Controller: `RefrigeratorController.scanReceipt()`
- Service: `RefrigeratorService.scanReceipt()`
- DTO: `ScanReceiptResponse` with nested `ScannedItem`

**Workflow**:
```
1. User uploads receipt image (JPG/PNG)
   ↓
2. Tesseract OCR extracts text
   ↓
3. ReceiptParserService parses items (regex)
   ↓
4. Returns parsed items for user review
   ↓
5. User edits/confirms list
   ↓
6. Calls POST /items/bulk to save
```

**Request**:
```bash
POST /api/v1/refrigerator/scan/receipt
Content-Type: multipart/form-data
Authorization: Bearer {token}

image: [receipt.jpg]
```

**Response**:
```json
{
  "extractedText": "이마트 영수증\n우유 2,500원\n계란 5,000원...",
  "scannedItems": [
    {
      "name": "우유",
      "quantity": 1,
      "unit": null,
      "price": 2500
    },
    {
      "name": "계란",
      "quantity": 1,
      "unit": null,
      "price": 5000
    }
  ],
  "totalItemsFound": 2
}
```

**Design Decisions**:
- ✅ **No auto-save**: Returns data for user review
- ✅ **Price included**: Reference only (not persisted)
- ✅ **Separate from OcrPipelineService**: Avoids circular dependency
- ✅ **Direct OCR integration**: Calls `TesseractOcrService` + `ReceiptParserService` directly

**Files Modified**:
- `RefrigeratorController.java` - Added endpoint with Swagger docs
- `RefrigeratorService.java` - Added scanReceipt() method
- `RefrigeratorDto.java` - Added ScanReceiptResponse DTO

**Tests**: All 18 refrigerator service tests pass ✅

---

### REF-05: 식재료 정보 수정 (#49)

**API**: `PUT /api/v1/refrigerator/items/{id}`

**Features**:
- Permission check (only owner can update)
- Partial update support
- All fields editable

---

### REF-06: 식재료 삭제 (#50)

**API**: `DELETE /api/v1/refrigerator/items/{id}`

**Features**:
- Permission check (only owner can delete)
- Soft/hard delete (currently hard delete)

---

## ❌ Not Implemented Features

### REF-04: 구매 내역으로 식재료 추가 (#48)

**Status**: ❌ Not Implemented  
**Complexity**: HIGH

**Potential Approaches**:

1. **Screenshot OCR** (Similar to REF-03)
   - User uploads purchase history screenshot
   - OCR extracts item names
   - Complexity: Medium

2. **E-commerce API Integration**
   - Integrate with Coupang/Gmarket/Naver Shopping APIs
   - Fetch order history programmatically
   - Complexity: Very High
   - Requires: API keys, OAuth, data agreements

**Recommendation**: **DEFER or use Screenshot approach**
- REF-03 (receipt scanning) already provides similar value
- E-commerce APIs are complex and may require business agreements
- Screenshot OCR reuses REF-03 infrastructure

---

### REF-07: 보유 재료 기반 레시피 추천 (#51)

**Status**: ❌ Not Implemented  
**Complexity**: MEDIUM

**Proposed API**: `GET /api/v1/refrigerator/recommendations?limit=10`

**Algorithm**:
```java
1. Fetch user's refrigerator items (names only)
2. Query all recipes with ingredient lists
3. For each recipe:
   - Calculate match rate = (matched ingredients / total ingredients) * 100
   - Identify missing ingredients
4. Sort by match rate DESC
5. Return top N recipes
```

**Response Structure**:
```json
{
  "recommendations": [
    {
      "recipeId": 123,
      "recipeName": "토마토 파스타",
      "matchRate": 80,
      "matchedIngredients": ["토마토", "마늘", "올리브유"],
      "missingIngredients": ["파스타 면"],
      "estimatedCookTime": 30
    }
  ]
}
```

**Requirements**:
- Recipe entity with ingredients relationship
- Ingredient name normalization (e.g., "대파" vs "파")
- Performance optimization for large recipe sets

**Implementation Priority**: **MEDIUM**
- High user value (helps users decide what to cook)
- Requires Recipe module to be complete first

---

### REF-08: 레시피 사용 재료 자동 차감 (#73)

**Status**: ❌ Not Implemented  
**Complexity**: MEDIUM-HIGH

**Proposed API Flow**:

**1. Preview Deduction**:
```
GET /api/v1/refrigerator/deduct-preview?recipeId=123
```

**Response**:
```json
{
  "recipeId": 123,
  "recipeName": "김치찌개",
  "ingredients": [
    {
      "name": "김치",
      "requiredAmount": "200g",
      "currentAmount": "500g",
      "status": "OK"
    },
    {
      "name": "돼지고기",
      "requiredAmount": "150g",
      "currentAmount": "100g",
      "status": "INSUFFICIENT"
    },
    {
      "name": "두부",
      "requiredAmount": "1개",
      "currentAmount": null,
      "status": "NOT_FOUND"
    }
  ],
  "canProceed": false,
  "warnings": ["돼지고기 부족 (50g)", "두부 없음"]
}
```

**2. Confirm Deduction**:
```
POST /api/v1/refrigerator/deduct
{
  "recipeId": 123,
  "confirmed": true,
  "ignoreWarnings": false
}
```

**Features**:
- Name matching (exact or fuzzy)
- Unit conversion (e.g., "200g" vs "0.2kg")
- Quantity validation
- Transaction safety (all-or-nothing update)
- Zero-quantity handling (delete or keep with 0?)

**Edge Cases**:
- Concurrent recipe cooking (optimistic locking)
- Partial quantities (e.g., 1.5 eggs → round up?)
- Multi-ingredient matching (e.g., "양파" matches "양파즙"?)

**Implementation Priority**: **LOW**
- Depends on REF-07 completion
- Complex business logic
- Lower user value than recommendations

---

## 🏗️ Technical Architecture

### Database Schema

```sql
CREATE TABLE refrigerator_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    quantity INT DEFAULT 1 CHECK (quantity >= 0),
    unit VARCHAR(10),
    expiration_date DATE,
    memo VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_member_name (member_id, name),
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);
```

### Domain Model

```java
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "name"}))
public class RefrigeratorItem {
    private Long id;
    private Member member;
    private String name;           // max 50, unique per member
    private Integer quantity;      // default 1, min 0
    private String unit;          // max 10
    private LocalDate expirationDate;
    private String memo;          // max 200
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Business logic
    public Long getDaysUntilExpiration();  // D-day calculation
    public boolean isExpirationSoon();     // <= 3 days
    public boolean isExpired();            // < 0 days
}
```

### API Endpoints Summary

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/refrigerator/items` | List items with sorting | JWT |
| POST | `/api/v1/refrigerator/items` | Add single item | JWT |
| POST | `/api/v1/refrigerator/items/bulk` | Bulk add items | JWT |
| POST | `/api/v1/refrigerator/scan/receipt` | ✨ Scan receipt OCR | JWT |
| PUT | `/api/v1/refrigerator/items/{id}` | Update item | JWT |
| DELETE | `/api/v1/refrigerator/items/{id}` | Delete item | JWT |

### Service Dependencies

```
RefrigeratorService
├── RefrigeratorItemRepository (JPA)
├── MemberRepository
├── TesseractOcrService (for REF-03)
└── ReceiptParserService (for REF-03)

OcrPipelineService (separate workflow)
├── TesseractOcrService
├── ReceiptParserService
└── RefrigeratorService (for auto-add workflow)
```

**Design Note**: REF-03 directly uses OCR services instead of `OcrPipelineService` to avoid circular dependency and provide user review workflow.

---

## 📝 Code Conventions (from PR #145)

### Swagger Documentation
- All endpoints have `@Operation` with summary/description
- Request/response examples included
- Error responses documented (400, 401, 404)

### Exception Handling
- Custom exceptions: `ItemNotFoundException`, `DuplicateItemException`, `UnauthorizedItemAccessException`
- Global exception handler returns standardized error responses

### Validation
- `@Valid` on request DTOs
- `@NotBlank`, `@Size`, `@Min`, `@Max` on fields
- Unique constraint enforced at DB level

### Logging
- INFO: Successful operations with key parameters
- ERROR: Exceptions with stack traces
- Pattern: `"Operation: key1={}, key2={}", value1, value2`

### Testing
- Service tests: Mock dependencies, verify logic
- Controller tests: MockMvc, verify HTTP responses
- 100% coverage of CRUD operations

---

## 🚀 Next Steps

### Immediate (REF-03 완료)
- [x] Add scanReceipt() service method ✅
- [x] Integrate with TesseractOcrService + ReceiptParserService ✅
- [x] Create ScanReceiptResponse DTO ✅
- [x] Run tests ✅ (18/18 passed)

### Short-term (High Priority)
- [ ] Implement REF-07 (Recipe Recommendations)
  - Requires Recipe module completion
  - Medium complexity, high user value
- [ ] Add integration tests for REF-03
  - Test with real receipt images
  - Verify OCR accuracy

### Medium-term (Lower Priority)
- [ ] Implement REF-08 (Auto-deduct Ingredients)
  - Depends on REF-07
  - Complex business logic
- [ ] Consider REF-04 (Purchase History)
  - Evaluate screenshot OCR approach
  - Or defer if REF-03 provides sufficient value

### Documentation
- [ ] Update API documentation
- [ ] Add user guide for receipt scanning workflow
- [ ] Document OCR accuracy limitations

---

## 📊 Test Results

**RefrigeratorServiceTest**: ✅ 18/18 tests passed

```
✓ getMyItems_정상조회
✓ getMyItems_소비기한순정렬
✓ getMyItems_이름순정렬
✓ getMyItems_등록일순정렬
✓ addItem_정상등록
✓ addItem_중복예외
✓ addItem_기본값설정
✓ addItemsBulk_정상등록
✓ addItemsBulk_중복스킵
✓ addItemsBulk_부분성공
✓ updateItem_정상수정
✓ updateItem_권한없음
✓ updateItem_존재하지않음
✓ deleteItem_정상삭제
✓ deleteItem_권한없음
✓ deleteItem_존재하지않음
✓ getDaysUntilExpiration_계산
✓ isExpirationSoon_3일이내
```

---

## 📌 Important Notes

1. **Receipt Scanning Workflow**:
   - REF-03 does NOT auto-save items
   - Returns parsed data for user review
   - User must call `/items/bulk` to save
   - Prevents OCR errors from corrupting data

2. **Duplicate Handling**:
   - Single add: Throws exception
   - Bulk add: Skips duplicates silently
   - Unique constraint: (member_id, name)

3. **Expiration Management**:
   - D-day calculated dynamically
   - "Soon" threshold: 3 days
   - Expired items not auto-deleted (user decision)

4. **Security**:
   - All endpoints require JWT authentication
   - Permission checks on update/delete
   - Member ID extracted from JWT token

---

**Last Updated**: 2024-05-12  
**Author**: GitHub Copilot  
**Status**: 5/8 features complete (62.5%)
