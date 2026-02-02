package vip.mystery0.xhu.timetable.shared.database

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Double
import kotlin.Long
import kotlin.String

public class SchemaQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectUser(studentId: String, mapper: (
    studentId: String,
    tokenEncrypted: String,
    name: String,
    gender: String,
    xhuGrade: Long,
    college: String,
    majorName: String,
    className: String,
    majorDirection: String,
  ) -> T): Query<T> = SelectUserQuery(studentId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getString(8)!!
    )
  }

  public fun selectUser(studentId: String): Query<User> = selectUser(studentId) { studentId_,
      tokenEncrypted, name, gender, xhuGrade, college, majorName, className, majorDirection ->
    User(
      studentId_,
      tokenEncrypted,
      name,
      gender,
      xhuGrade,
      college,
      majorName,
      className,
      majorDirection
    )
  }

  public fun <T : Any> selectAllUsers(mapper: (
    studentId: String,
    tokenEncrypted: String,
    name: String,
    gender: String,
    xhuGrade: Long,
    college: String,
    majorName: String,
    className: String,
    majorDirection: String,
  ) -> T): Query<T> = Query(-747_317_590, arrayOf("User"), driver, "Schema.sq", "selectAllUsers",
      """
  |SELECT User.studentId, User.tokenEncrypted, User.name, User.gender, User.xhuGrade, User.college, User.majorName, User.className, User.majorDirection FROM User
  |ORDER BY studentId
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getString(8)!!
    )
  }

  public fun selectAllUsers(): Query<User> = selectAllUsers { studentId, tokenEncrypted, name,
      gender, xhuGrade, college, majorName, className, majorDirection ->
    User(
      studentId,
      tokenEncrypted,
      name,
      gender,
      xhuGrade,
      college,
      majorName,
      className,
      majorDirection
    )
  }

  public fun <T : Any> selectSelectedTerm(studentId: String, mapper: (
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ) -> T): Query<T> = SelectSelectedTermQuery(studentId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!
    )
  }

  public fun selectSelectedTerm(studentId: String): Query<SelectedTerm> =
      selectSelectedTerm(studentId) { studentId_, termYear, termIndex ->
    SelectedTerm(
      studentId_,
      termYear,
      termIndex
    )
  }

  public fun <T : Any> selectSyncState(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    mapper: (
      studentId: String,
      termYear: Long,
      termIndex: Long,
      lastSyncAt: Long,
    ) -> T,
  ): Query<T> = SelectSyncStateQuery(studentId, termYear, termIndex) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!
    )
  }

  public fun selectSyncState(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ): Query<SyncState> = selectSyncState(studentId, termYear, termIndex) { studentId_, termYear_,
      termIndex_, lastSyncAt ->
    SyncState(
      studentId_,
      termYear_,
      termIndex_,
      lastSyncAt
    )
  }

  public fun selectLastSyncAt(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ): Query<Long> = SelectLastSyncAtQuery(studentId, termYear, termIndex) { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> selectCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    mapper: (
      id: Long,
      studentId: String,
      termYear: Long,
      termIndex: Long,
      courseName: String,
      weekStr: String,
      weekList: String,
      day: Long,
      dayIndex: Long,
      startDayTime: Long,
      endDayTime: Long,
      startTime: String,
      endTime: String,
      location: String,
      teacher: String,
      extraData: String,
      credit: Double,
      courseType: String,
      courseCodeType: String,
      courseCodeFlag: String,
      campus: String,
    ) -> T,
  ): Query<T> = SelectCoursesByPartitionQuery(studentId, termYear, termIndex) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getString(11)!!,
      cursor.getString(12)!!,
      cursor.getString(13)!!,
      cursor.getString(14)!!,
      cursor.getString(15)!!,
      cursor.getDouble(16)!!,
      cursor.getString(17)!!,
      cursor.getString(18)!!,
      cursor.getString(19)!!,
      cursor.getString(20)!!
    )
  }

  public fun selectCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ): Query<Course> = selectCoursesByPartition(studentId, termYear, termIndex) { id, studentId_,
      termYear_, termIndex_, courseName, weekStr, weekList, day, dayIndex, startDayTime, endDayTime,
      startTime, endTime, location, teacher, extraData, credit, courseType, courseCodeType,
      courseCodeFlag, campus ->
    Course(
      id,
      studentId_,
      termYear_,
      termIndex_,
      courseName,
      weekStr,
      weekList,
      day,
      dayIndex,
      startDayTime,
      endDayTime,
      startTime,
      endTime,
      location,
      teacher,
      extraData,
      credit,
      courseType,
      courseCodeType,
      courseCodeFlag,
      campus
    )
  }

  public fun <T : Any> selectCoursesByPartitionAndDay(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    dayIndex: Long,
    mapper: (
      id: Long,
      studentId: String,
      termYear: Long,
      termIndex: Long,
      courseName: String,
      weekStr: String,
      weekList: String,
      day: Long,
      dayIndex: Long,
      startDayTime: Long,
      endDayTime: Long,
      startTime: String,
      endTime: String,
      location: String,
      teacher: String,
      extraData: String,
      credit: Double,
      courseType: String,
      courseCodeType: String,
      courseCodeFlag: String,
      campus: String,
    ) -> T,
  ): Query<T> = SelectCoursesByPartitionAndDayQuery(studentId, termYear, termIndex, dayIndex) {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getString(11)!!,
      cursor.getString(12)!!,
      cursor.getString(13)!!,
      cursor.getString(14)!!,
      cursor.getString(15)!!,
      cursor.getDouble(16)!!,
      cursor.getString(17)!!,
      cursor.getString(18)!!,
      cursor.getString(19)!!,
      cursor.getString(20)!!
    )
  }

  public fun selectCoursesByPartitionAndDay(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    dayIndex: Long,
  ): Query<Course> = selectCoursesByPartitionAndDay(studentId, termYear, termIndex, dayIndex) { id,
      studentId_, termYear_, termIndex_, courseName, weekStr, weekList, day, dayIndex_,
      startDayTime, endDayTime, startTime, endTime, location, teacher, extraData, credit,
      courseType, courseCodeType, courseCodeFlag, campus ->
    Course(
      id,
      studentId_,
      termYear_,
      termIndex_,
      courseName,
      weekStr,
      weekList,
      day,
      dayIndex_,
      startDayTime,
      endDayTime,
      startTime,
      endTime,
      location,
      teacher,
      extraData,
      credit,
      courseType,
      courseCodeType,
      courseCodeFlag,
      campus
    )
  }

  public fun <T : Any> selectCourseById(id: Long, mapper: (
    id: Long,
    studentId: String,
    termYear: Long,
    termIndex: Long,
    courseName: String,
    weekStr: String,
    weekList: String,
    day: Long,
    dayIndex: Long,
    startDayTime: Long,
    endDayTime: Long,
    startTime: String,
    endTime: String,
    location: String,
    teacher: String,
    extraData: String,
    credit: Double,
    courseType: String,
    courseCodeType: String,
    courseCodeFlag: String,
    campus: String,
  ) -> T): Query<T> = SelectCourseByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getString(11)!!,
      cursor.getString(12)!!,
      cursor.getString(13)!!,
      cursor.getString(14)!!,
      cursor.getString(15)!!,
      cursor.getDouble(16)!!,
      cursor.getString(17)!!,
      cursor.getString(18)!!,
      cursor.getString(19)!!,
      cursor.getString(20)!!
    )
  }

  public fun selectCourseById(id: Long): Query<Course> = selectCourseById(id) { id_, studentId,
      termYear, termIndex, courseName, weekStr, weekList, day, dayIndex, startDayTime, endDayTime,
      startTime, endTime, location, teacher, extraData, credit, courseType, courseCodeType,
      courseCodeFlag, campus ->
    Course(
      id_,
      studentId,
      termYear,
      termIndex,
      courseName,
      weekStr,
      weekList,
      day,
      dayIndex,
      startDayTime,
      endDayTime,
      startTime,
      endTime,
      location,
      teacher,
      extraData,
      credit,
      courseType,
      courseCodeType,
      courseCodeFlag,
      campus
    )
  }

  public fun countCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ): Query<Long> = CountCoursesByPartitionQuery(studentId, termYear, termIndex) { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> selectExperimentCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    mapper: (
      id: Long,
      studentId: String,
      termYear: Long,
      termIndex: Long,
      courseName: String,
      experimentProjectName: String,
      experimentGroupName: String,
      weekStr: String,
      weekList: String,
      day: Long,
      dayIndex: Long,
      startDayTime: Long,
      endDayTime: Long,
      startTime: String,
      endTime: String,
      location: String,
      teacherName: String,
      region: String,
    ) -> T,
  ): Query<T> = SelectExperimentCoursesByPartitionQuery(studentId, termYear, termIndex) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getString(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!,
      cursor.getLong(12)!!,
      cursor.getString(13)!!,
      cursor.getString(14)!!,
      cursor.getString(15)!!,
      cursor.getString(16)!!,
      cursor.getString(17)!!
    )
  }

  public fun selectExperimentCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ): Query<ExperimentCourse> = selectExperimentCoursesByPartition(studentId, termYear, termIndex) {
      id, studentId_, termYear_, termIndex_, courseName, experimentProjectName, experimentGroupName,
      weekStr, weekList, day, dayIndex, startDayTime, endDayTime, startTime, endTime, location,
      teacherName, region ->
    ExperimentCourse(
      id,
      studentId_,
      termYear_,
      termIndex_,
      courseName,
      experimentProjectName,
      experimentGroupName,
      weekStr,
      weekList,
      day,
      dayIndex,
      startDayTime,
      endDayTime,
      startTime,
      endTime,
      location,
      teacherName,
      region
    )
  }

  public fun <T : Any> selectExperimentCoursesByPartitionAndDay(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    dayIndex: Long,
    mapper: (
      id: Long,
      studentId: String,
      termYear: Long,
      termIndex: Long,
      courseName: String,
      experimentProjectName: String,
      experimentGroupName: String,
      weekStr: String,
      weekList: String,
      day: Long,
      dayIndex: Long,
      startDayTime: Long,
      endDayTime: Long,
      startTime: String,
      endTime: String,
      location: String,
      teacherName: String,
      region: String,
    ) -> T,
  ): Query<T> = SelectExperimentCoursesByPartitionAndDayQuery(studentId, termYear, termIndex,
      dayIndex) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getString(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!,
      cursor.getLong(12)!!,
      cursor.getString(13)!!,
      cursor.getString(14)!!,
      cursor.getString(15)!!,
      cursor.getString(16)!!,
      cursor.getString(17)!!
    )
  }

  public fun selectExperimentCoursesByPartitionAndDay(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    dayIndex: Long,
  ): Query<ExperimentCourse> = selectExperimentCoursesByPartitionAndDay(studentId, termYear,
      termIndex, dayIndex) { id, studentId_, termYear_, termIndex_, courseName,
      experimentProjectName, experimentGroupName, weekStr, weekList, day, dayIndex_, startDayTime,
      endDayTime, startTime, endTime, location, teacherName, region ->
    ExperimentCourse(
      id,
      studentId_,
      termYear_,
      termIndex_,
      courseName,
      experimentProjectName,
      experimentGroupName,
      weekStr,
      weekList,
      day,
      dayIndex_,
      startDayTime,
      endDayTime,
      startTime,
      endTime,
      location,
      teacherName,
      region
    )
  }

  public fun <T : Any> selectExperimentCourseById(id: Long, mapper: (
    id: Long,
    studentId: String,
    termYear: Long,
    termIndex: Long,
    courseName: String,
    experimentProjectName: String,
    experimentGroupName: String,
    weekStr: String,
    weekList: String,
    day: Long,
    dayIndex: Long,
    startDayTime: Long,
    endDayTime: Long,
    startTime: String,
    endTime: String,
    location: String,
    teacherName: String,
    region: String,
  ) -> T): Query<T> = SelectExperimentCourseByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getString(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!,
      cursor.getLong(12)!!,
      cursor.getString(13)!!,
      cursor.getString(14)!!,
      cursor.getString(15)!!,
      cursor.getString(16)!!,
      cursor.getString(17)!!
    )
  }

  public fun selectExperimentCourseById(id: Long): Query<ExperimentCourse> =
      selectExperimentCourseById(id) { id_, studentId, termYear, termIndex, courseName,
      experimentProjectName, experimentGroupName, weekStr, weekList, day, dayIndex, startDayTime,
      endDayTime, startTime, endTime, location, teacherName, region ->
    ExperimentCourse(
      id_,
      studentId,
      termYear,
      termIndex,
      courseName,
      experimentProjectName,
      experimentGroupName,
      weekStr,
      weekList,
      day,
      dayIndex,
      startDayTime,
      endDayTime,
      startTime,
      endTime,
      location,
      teacherName,
      region
    )
  }

  public fun countExperimentCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ): Query<Long> = CountExperimentCoursesByPartitionQuery(studentId, termYear, termIndex) {
      cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> selectPracticalCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    mapper: (
      id: Long,
      studentId: String,
      termYear: Long,
      termIndex: Long,
      courseName: String,
      weekStr: String,
      weekList: String,
      credit: Double,
      teacher: String,
    ) -> T,
  ): Query<T> = SelectPracticalCoursesByPartitionQuery(studentId, termYear, termIndex) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getDouble(7)!!,
      cursor.getString(8)!!
    )
  }

  public fun selectPracticalCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ): Query<PracticalCourse> = selectPracticalCoursesByPartition(studentId, termYear, termIndex) {
      id, studentId_, termYear_, termIndex_, courseName, weekStr, weekList, credit, teacher ->
    PracticalCourse(
      id,
      studentId_,
      termYear_,
      termIndex_,
      courseName,
      weekStr,
      weekList,
      credit,
      teacher
    )
  }

  public fun <T : Any> selectPracticalCourseById(id: Long, mapper: (
    id: Long,
    studentId: String,
    termYear: Long,
    termIndex: Long,
    courseName: String,
    weekStr: String,
    weekList: String,
    credit: Double,
    teacher: String,
  ) -> T): Query<T> = SelectPracticalCourseByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getDouble(7)!!,
      cursor.getString(8)!!
    )
  }

  public fun selectPracticalCourseById(id: Long): Query<PracticalCourse> =
      selectPracticalCourseById(id) { id_, studentId, termYear, termIndex, courseName, weekStr,
      weekList, credit, teacher ->
    PracticalCourse(
      id_,
      studentId,
      termYear,
      termIndex,
      courseName,
      weekStr,
      weekList,
      credit,
      teacher
    )
  }

  public fun countPracticalCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ): Query<Long> = CountPracticalCoursesByPartitionQuery(studentId, termYear, termIndex) { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> selectCustomCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    mapper: (
      studentId: String,
      termYear: Long,
      termIndex: Long,
      courseId: Long,
      courseName: String,
      weekStr: String,
      weekList: String,
      day: Long,
      dayIndex: Long,
      startDayTime: Long,
      endDayTime: Long,
      startTime: String,
      endTime: String,
      location: String,
      teacher: String,
      extraData: String,
      createTime: Long,
    ) -> T,
  ): Query<T> = SelectCustomCoursesByPartitionQuery(studentId, termYear, termIndex) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getString(11)!!,
      cursor.getString(12)!!,
      cursor.getString(13)!!,
      cursor.getString(14)!!,
      cursor.getString(15)!!,
      cursor.getLong(16)!!
    )
  }

  public fun selectCustomCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ): Query<CustomCourse> = selectCustomCoursesByPartition(studentId, termYear, termIndex) {
      studentId_, termYear_, termIndex_, courseId, courseName, weekStr, weekList, day, dayIndex,
      startDayTime, endDayTime, startTime, endTime, location, teacher, extraData, createTime ->
    CustomCourse(
      studentId_,
      termYear_,
      termIndex_,
      courseId,
      courseName,
      weekStr,
      weekList,
      day,
      dayIndex,
      startDayTime,
      endDayTime,
      startTime,
      endTime,
      location,
      teacher,
      extraData,
      createTime
    )
  }

  public fun <T : Any> selectCustomCoursesByPartitionAndDay(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    dayIndex: Long,
    mapper: (
      studentId: String,
      termYear: Long,
      termIndex: Long,
      courseId: Long,
      courseName: String,
      weekStr: String,
      weekList: String,
      day: Long,
      dayIndex: Long,
      startDayTime: Long,
      endDayTime: Long,
      startTime: String,
      endTime: String,
      location: String,
      teacher: String,
      extraData: String,
      createTime: Long,
    ) -> T,
  ): Query<T> = SelectCustomCoursesByPartitionAndDayQuery(studentId, termYear, termIndex,
      dayIndex) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getString(11)!!,
      cursor.getString(12)!!,
      cursor.getString(13)!!,
      cursor.getString(14)!!,
      cursor.getString(15)!!,
      cursor.getLong(16)!!
    )
  }

  public fun selectCustomCoursesByPartitionAndDay(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    dayIndex: Long,
  ): Query<CustomCourse> = selectCustomCoursesByPartitionAndDay(studentId, termYear, termIndex,
      dayIndex) { studentId_, termYear_, termIndex_, courseId, courseName, weekStr, weekList, day,
      dayIndex_, startDayTime, endDayTime, startTime, endTime, location, teacher, extraData,
      createTime ->
    CustomCourse(
      studentId_,
      termYear_,
      termIndex_,
      courseId,
      courseName,
      weekStr,
      weekList,
      day,
      dayIndex_,
      startDayTime,
      endDayTime,
      startTime,
      endTime,
      location,
      teacher,
      extraData,
      createTime
    )
  }

  public fun <T : Any> selectCustomCourseById(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    courseId: Long,
    mapper: (
      studentId: String,
      termYear: Long,
      termIndex: Long,
      courseId: Long,
      courseName: String,
      weekStr: String,
      weekList: String,
      day: Long,
      dayIndex: Long,
      startDayTime: Long,
      endDayTime: Long,
      startTime: String,
      endTime: String,
      location: String,
      teacher: String,
      extraData: String,
      createTime: Long,
    ) -> T,
  ): Query<T> = SelectCustomCourseByIdQuery(studentId, termYear, termIndex, courseId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getString(11)!!,
      cursor.getString(12)!!,
      cursor.getString(13)!!,
      cursor.getString(14)!!,
      cursor.getString(15)!!,
      cursor.getLong(16)!!
    )
  }

  public fun selectCustomCourseById(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    courseId: Long,
  ): Query<CustomCourse> = selectCustomCourseById(studentId, termYear, termIndex, courseId) {
      studentId_, termYear_, termIndex_, courseId_, courseName, weekStr, weekList, day, dayIndex,
      startDayTime, endDayTime, startTime, endTime, location, teacher, extraData, createTime ->
    CustomCourse(
      studentId_,
      termYear_,
      termIndex_,
      courseId_,
      courseName,
      weekStr,
      weekList,
      day,
      dayIndex,
      startDayTime,
      endDayTime,
      startTime,
      endTime,
      location,
      teacher,
      extraData,
      createTime
    )
  }

  public fun countCustomCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ): Query<Long> = CountCustomCoursesByPartitionQuery(studentId, termYear, termIndex) { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> selectCustomThingsByStudent(studentId: String, mapper: (
    studentId: String,
    thingId: Long,
    title: String,
    location: String,
    allDay: Long,
    startTime: Long,
    endTime: Long,
    remark: String,
    color: String,
    metadata: String,
    createTime: Long,
  ) -> T): Query<T> = SelectCustomThingsByStudentQuery(studentId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getLong(6)!!,
      cursor.getString(7)!!,
      cursor.getString(8)!!,
      cursor.getString(9)!!,
      cursor.getLong(10)!!
    )
  }

  public fun selectCustomThingsByStudent(studentId: String): Query<CustomThing> =
      selectCustomThingsByStudent(studentId) { studentId_, thingId, title, location, allDay,
      startTime, endTime, remark, color, metadata, createTime ->
    CustomThing(
      studentId_,
      thingId,
      title,
      location,
      allDay,
      startTime,
      endTime,
      remark,
      color,
      metadata,
      createTime
    )
  }

  public fun <T : Any> selectCustomThingById(
    studentId: String,
    thingId: Long,
    mapper: (
      studentId: String,
      thingId: Long,
      title: String,
      location: String,
      allDay: Long,
      startTime: Long,
      endTime: Long,
      remark: String,
      color: String,
      metadata: String,
      createTime: Long,
    ) -> T,
  ): Query<T> = SelectCustomThingByIdQuery(studentId, thingId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getLong(6)!!,
      cursor.getString(7)!!,
      cursor.getString(8)!!,
      cursor.getString(9)!!,
      cursor.getLong(10)!!
    )
  }

  public fun selectCustomThingById(studentId: String, thingId: Long): Query<CustomThing> =
      selectCustomThingById(studentId, thingId) { studentId_, thingId_, title, location, allDay,
      startTime, endTime, remark, color, metadata, createTime ->
    CustomThing(
      studentId_,
      thingId_,
      title,
      location,
      allDay,
      startTime,
      endTime,
      remark,
      color,
      metadata,
      createTime
    )
  }

  public fun selectSetting(
    scope: String,
    scopeId: String,
    name: String,
  ): Query<String> = SelectSettingQuery(scope, scopeId, name) { cursor ->
    cursor.getString(0)!!
  }

  public fun <T : Any> selectSettingsByScope(
    scope: String,
    scopeId: String,
    mapper: (
      scope: String,
      scopeId: String,
      name: String,
      value_: String,
      updatedAt: Long,
    ) -> T,
  ): Query<T> = SelectSettingsByScopeQuery(scope, scopeId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!
    )
  }

  public fun selectSettingsByScope(scope: String, scopeId: String): Query<Setting> =
      selectSettingsByScope(scope, scopeId) { scope_, scopeId_, name, value_, updatedAt ->
    Setting(
      scope_,
      scopeId_,
      name,
      value_,
      updatedAt
    )
  }

  public fun selectCourseColor(studentId: String, courseName: String): Query<String> =
      SelectCourseColorQuery(studentId, courseName) { cursor ->
    cursor.getString(0)!!
  }

  public fun <T : Any> selectCourseColorsByStudent(studentId: String, mapper: (
    studentId: String,
    courseName: String,
    colorHex: String,
    updatedAt: Long,
  ) -> T): Query<T> = SelectCourseColorsByStudentQuery(studentId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!
    )
  }

  public fun selectCourseColorsByStudent(studentId: String): Query<CourseColor> =
      selectCourseColorsByStudent(studentId) { studentId_, courseName, colorHex, updatedAt ->
    CourseColor(
      studentId_,
      courseName,
      colorHex,
      updatedAt
    )
  }

  public fun <T : Any> selectNoticeById(noticeId: Long, mapper: (
    noticeId: Long,
    title: String,
    content: String,
    actionsJson: String,
    released: Long,
    createTime: Long,
    updateTime: Long,
  ) -> T): Query<T> = SelectNoticeByIdQuery(noticeId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getLong(6)!!
    )
  }

  public fun selectNoticeById(noticeId: Long): Query<Notice> = selectNoticeById(noticeId) {
      noticeId_, title, content, actionsJson, released, createTime, updateTime ->
    Notice(
      noticeId_,
      title,
      content,
      actionsJson,
      released,
      createTime,
      updateTime
    )
  }

  public fun <T : Any> selectAllNotices(mapper: (
    noticeId: Long,
    title: String,
    content: String,
    actionsJson: String,
    released: Long,
    createTime: Long,
    updateTime: Long,
  ) -> T): Query<T> = Query(1_363_799_229, arrayOf("Notice"), driver, "Schema.sq",
      "selectAllNotices", """
  |SELECT Notice.noticeId, Notice.title, Notice.content, Notice.actionsJson, Notice.released, Notice.createTime, Notice.updateTime FROM Notice
  |ORDER BY updateTime DESC
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getLong(6)!!
    )
  }

  public fun selectAllNotices(): Query<Notice> = selectAllNotices { noticeId, title, content,
      actionsJson, released, createTime, updateTime ->
    Notice(
      noticeId,
      title,
      content,
      actionsJson,
      released,
      createTime,
      updateTime
    )
  }

  public fun <T : Any> selectNoticesPaged(
    limit: Long,
    offset: Long,
    mapper: (
      noticeId: Long,
      title: String,
      content: String,
      actionsJson: String,
      released: Long,
      createTime: Long,
      updateTime: Long,
    ) -> T,
  ): Query<T> = SelectNoticesPagedQuery(limit, offset) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getLong(6)!!
    )
  }

  public fun selectNoticesPaged(limit: Long, offset: Long): Query<Notice> =
      selectNoticesPaged(limit, offset) { noticeId, title, content, actionsJson, released,
      createTime, updateTime ->
    Notice(
      noticeId,
      title,
      content,
      actionsJson,
      released,
      createTime,
      updateTime
    )
  }

  public fun selectLatestNoticeId(): Query<Long> = Query(273_022_525, arrayOf("Notice"), driver,
      "Schema.sq", "selectLatestNoticeId", "SELECT COALESCE(MAX(noticeId), 0) FROM Notice") {
      cursor ->
    cursor.getLong(0)!!
  }

  public fun countNotices(): Query<Long> = Query(-2_060_123_405, arrayOf("Notice"), driver,
      "Schema.sq", "countNotices", "SELECT COUNT(*) FROM Notice") { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> selectBackgroundById(backgroundId: Long, mapper: (
    backgroundId: Long,
    resourceId: Long,
    thumbnailUrl: String,
    imageUrl: String,
    updatedAt: Long,
  ) -> T): Query<T> = SelectBackgroundByIdQuery(backgroundId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!
    )
  }

  public fun selectBackgroundById(backgroundId: Long): Query<Background> =
      selectBackgroundById(backgroundId) { backgroundId_, resourceId, thumbnailUrl, imageUrl,
      updatedAt ->
    Background(
      backgroundId_,
      resourceId,
      thumbnailUrl,
      imageUrl,
      updatedAt
    )
  }

  public fun <T : Any> selectAllBackgrounds(mapper: (
    backgroundId: Long,
    resourceId: Long,
    thumbnailUrl: String,
    imageUrl: String,
    updatedAt: Long,
  ) -> T): Query<T> = Query(387_717_159, arrayOf("Background"), driver, "Schema.sq",
      "selectAllBackgrounds", """
  |SELECT Background.backgroundId, Background.resourceId, Background.thumbnailUrl, Background.imageUrl, Background.updatedAt FROM Background
  |ORDER BY backgroundId
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!
    )
  }

  public fun selectAllBackgrounds(): Query<Background> = selectAllBackgrounds { backgroundId,
      resourceId, thumbnailUrl, imageUrl, updatedAt ->
    Background(
      backgroundId,
      resourceId,
      thumbnailUrl,
      imageUrl,
      updatedAt
    )
  }

  public fun upsertUser(
    studentId: String,
    tokenEncrypted: String,
    name: String,
    gender: String,
    xhuGrade: Long,
    college: String,
    majorName: String,
    className: String,
    majorDirection: String,
  ) {
    driver.execute(1_078_832_577, """
        |INSERT OR REPLACE INTO User(
        |    studentId,
        |    tokenEncrypted,
        |    name,
        |    gender,
        |    xhuGrade,
        |    college,
        |    majorName,
        |    className,
        |    majorDirection
        |) VALUES (
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?
        |)
        """.trimMargin(), 9) {
          bindString(0, studentId)
          bindString(1, tokenEncrypted)
          bindString(2, name)
          bindString(3, gender)
          bindLong(4, xhuGrade)
          bindString(5, college)
          bindString(6, majorName)
          bindString(7, className)
          bindString(8, majorDirection)
        }
    notifyQueries(1_078_832_577) { emit ->
      emit("User")
    }
  }

  public fun deleteUser(studentId: String) {
    driver.execute(-739_607_491, """
        |DELETE FROM User
        |WHERE studentId = ?
        """.trimMargin(), 1) {
          bindString(0, studentId)
        }
    notifyQueries(-739_607_491) { emit ->
      emit("User")
    }
  }

  public fun upsertSelectedTerm(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ) {
    driver.execute(1_928_679_837, """
        |INSERT OR REPLACE INTO SelectedTerm(studentId, termYear, termIndex)
        |VALUES (?, ?, ?)
        """.trimMargin(), 3) {
          bindString(0, studentId)
          bindLong(1, termYear)
          bindLong(2, termIndex)
        }
    notifyQueries(1_928_679_837) { emit ->
      emit("SelectedTerm")
    }
  }

  public fun deleteSelectedTerm(studentId: String) {
    driver.execute(-1_928_731_623, """
        |DELETE FROM SelectedTerm
        |WHERE studentId = ?
        """.trimMargin(), 1) {
          bindString(0, studentId)
        }
    notifyQueries(-1_928_731_623) { emit ->
      emit("SelectedTerm")
    }
  }

  public fun upsertSyncState(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    lastSyncAt: Long,
  ) {
    driver.execute(1_972_578_944, """
        |INSERT OR REPLACE INTO SyncState(studentId, termYear, termIndex, lastSyncAt)
        |VALUES (?, ?, ?, ?)
        """.trimMargin(), 4) {
          bindString(0, studentId)
          bindLong(1, termYear)
          bindLong(2, termIndex)
          bindLong(3, lastSyncAt)
        }
    notifyQueries(1_972_578_944) { emit ->
      emit("SyncState")
    }
  }

  public fun deleteSyncState(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ) {
    driver.execute(492_833_156, """
        |DELETE FROM SyncState
        |WHERE studentId = ? AND termYear = ? AND termIndex = ?
        """.trimMargin(), 3) {
          bindString(0, studentId)
          bindLong(1, termYear)
          bindLong(2, termIndex)
        }
    notifyQueries(492_833_156) { emit ->
      emit("SyncState")
    }
  }

  public fun insertCourse(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    courseName: String,
    weekStr: String,
    weekList: String,
    day: Long,
    dayIndex: Long,
    startDayTime: Long,
    endDayTime: Long,
    startTime: String,
    endTime: String,
    location: String,
    teacher: String,
    extraData: String,
    credit: Double,
    courseType: String,
    courseCodeType: String,
    courseCodeFlag: String,
    campus: String,
  ) {
    driver.execute(-1_097_607_525, """
        |INSERT INTO Course(
        |    studentId,
        |    termYear,
        |    termIndex,
        |    courseName,
        |    weekStr,
        |    weekList,
        |    day,
        |    dayIndex,
        |    startDayTime,
        |    endDayTime,
        |    startTime,
        |    endTime,
        |    location,
        |    teacher,
        |    extraData,
        |    credit,
        |    courseType,
        |    courseCodeType,
        |    courseCodeFlag,
        |    campus
        |) VALUES (
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?
        |)
        """.trimMargin(), 20) {
          bindString(0, studentId)
          bindLong(1, termYear)
          bindLong(2, termIndex)
          bindString(3, courseName)
          bindString(4, weekStr)
          bindString(5, weekList)
          bindLong(6, day)
          bindLong(7, dayIndex)
          bindLong(8, startDayTime)
          bindLong(9, endDayTime)
          bindString(10, startTime)
          bindString(11, endTime)
          bindString(12, location)
          bindString(13, teacher)
          bindString(14, extraData)
          bindDouble(15, credit)
          bindString(16, courseType)
          bindString(17, courseCodeType)
          bindString(18, courseCodeFlag)
          bindString(19, campus)
        }
    notifyQueries(-1_097_607_525) { emit ->
      emit("Course")
    }
  }

  public fun updateCourseById(
    courseName: String,
    weekStr: String,
    weekList: String,
    day: Long,
    dayIndex: Long,
    startDayTime: Long,
    endDayTime: Long,
    startTime: String,
    endTime: String,
    location: String,
    teacher: String,
    extraData: String,
    credit: Double,
    courseType: String,
    courseCodeType: String,
    courseCodeFlag: String,
    campus: String,
    id: Long,
  ) {
    driver.execute(-1_277_164_771, """
        |UPDATE Course SET
        |    courseName = ?,
        |    weekStr = ?,
        |    weekList = ?,
        |    day = ?,
        |    dayIndex = ?,
        |    startDayTime = ?,
        |    endDayTime = ?,
        |    startTime = ?,
        |    endTime = ?,
        |    location = ?,
        |    teacher = ?,
        |    extraData = ?,
        |    credit = ?,
        |    courseType = ?,
        |    courseCodeType = ?,
        |    courseCodeFlag = ?,
        |    campus = ?
        |WHERE id = ?
        """.trimMargin(), 18) {
          bindString(0, courseName)
          bindString(1, weekStr)
          bindString(2, weekList)
          bindLong(3, day)
          bindLong(4, dayIndex)
          bindLong(5, startDayTime)
          bindLong(6, endDayTime)
          bindString(7, startTime)
          bindString(8, endTime)
          bindString(9, location)
          bindString(10, teacher)
          bindString(11, extraData)
          bindDouble(12, credit)
          bindString(13, courseType)
          bindString(14, courseCodeType)
          bindString(15, courseCodeFlag)
          bindString(16, campus)
          bindLong(17, id)
        }
    notifyQueries(-1_277_164_771) { emit ->
      emit("Course")
    }
  }

  public fun deleteCourseById(id: Long) {
    driver.execute(-1_514_332_801, """
        |DELETE FROM Course
        |WHERE id = ?
        """.trimMargin(), 1) {
          bindLong(0, id)
        }
    notifyQueries(-1_514_332_801) { emit ->
      emit("Course")
    }
  }

  public fun deleteCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ) {
    driver.execute(1_381_399_149, """
        |DELETE FROM Course
        |WHERE studentId = ? AND termYear = ? AND termIndex = ?
        """.trimMargin(), 3) {
          bindString(0, studentId)
          bindLong(1, termYear)
          bindLong(2, termIndex)
        }
    notifyQueries(1_381_399_149) { emit ->
      emit("Course")
    }
  }

  public fun insertExperimentCourse(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    courseName: String,
    experimentProjectName: String,
    experimentGroupName: String,
    weekStr: String,
    weekList: String,
    day: Long,
    dayIndex: Long,
    startDayTime: Long,
    endDayTime: Long,
    startTime: String,
    endTime: String,
    location: String,
    teacherName: String,
    region: String,
  ) {
    driver.execute(2_121_846_008, """
        |INSERT INTO ExperimentCourse(
        |    studentId,
        |    termYear,
        |    termIndex,
        |    courseName,
        |    experimentProjectName,
        |    experimentGroupName,
        |    weekStr,
        |    weekList,
        |    day,
        |    dayIndex,
        |    startDayTime,
        |    endDayTime,
        |    startTime,
        |    endTime,
        |    location,
        |    teacherName,
        |    region
        |) VALUES (
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?
        |)
        """.trimMargin(), 17) {
          bindString(0, studentId)
          bindLong(1, termYear)
          bindLong(2, termIndex)
          bindString(3, courseName)
          bindString(4, experimentProjectName)
          bindString(5, experimentGroupName)
          bindString(6, weekStr)
          bindString(7, weekList)
          bindLong(8, day)
          bindLong(9, dayIndex)
          bindLong(10, startDayTime)
          bindLong(11, endDayTime)
          bindString(12, startTime)
          bindString(13, endTime)
          bindString(14, location)
          bindString(15, teacherName)
          bindString(16, region)
        }
    notifyQueries(2_121_846_008) { emit ->
      emit("ExperimentCourse")
    }
  }

  public fun updateExperimentCourseById(
    courseName: String,
    experimentProjectName: String,
    experimentGroupName: String,
    weekStr: String,
    weekList: String,
    day: Long,
    dayIndex: Long,
    startDayTime: Long,
    endDayTime: Long,
    startTime: String,
    endTime: String,
    location: String,
    teacherName: String,
    region: String,
    id: Long,
  ) {
    driver.execute(1_287_728_890, """
        |UPDATE ExperimentCourse SET
        |    courseName = ?,
        |    experimentProjectName = ?,
        |    experimentGroupName = ?,
        |    weekStr = ?,
        |    weekList = ?,
        |    day = ?,
        |    dayIndex = ?,
        |    startDayTime = ?,
        |    endDayTime = ?,
        |    startTime = ?,
        |    endTime = ?,
        |    location = ?,
        |    teacherName = ?,
        |    region = ?
        |WHERE id = ?
        """.trimMargin(), 15) {
          bindString(0, courseName)
          bindString(1, experimentProjectName)
          bindString(2, experimentGroupName)
          bindString(3, weekStr)
          bindString(4, weekList)
          bindLong(5, day)
          bindLong(6, dayIndex)
          bindLong(7, startDayTime)
          bindLong(8, endDayTime)
          bindString(9, startTime)
          bindString(10, endTime)
          bindString(11, location)
          bindString(12, teacherName)
          bindString(13, region)
          bindLong(14, id)
        }
    notifyQueries(1_287_728_890) { emit ->
      emit("ExperimentCourse")
    }
  }

  public fun deleteExperimentCourseById(id: Long) {
    driver.execute(-2_012_072_228, """
        |DELETE FROM ExperimentCourse
        |WHERE id = ?
        """.trimMargin(), 1) {
          bindLong(0, id)
        }
    notifyQueries(-2_012_072_228) { emit ->
      emit("ExperimentCourse")
    }
  }

  public fun deleteExperimentCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ) {
    driver.execute(-1_688_279_350, """
        |DELETE FROM ExperimentCourse
        |WHERE studentId = ? AND termYear = ? AND termIndex = ?
        """.trimMargin(), 3) {
          bindString(0, studentId)
          bindLong(1, termYear)
          bindLong(2, termIndex)
        }
    notifyQueries(-1_688_279_350) { emit ->
      emit("ExperimentCourse")
    }
  }

  public fun insertPracticalCourse(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    courseName: String,
    weekStr: String,
    weekList: String,
    credit: Double,
    teacher: String,
  ) {
    driver.execute(1_183_393_808, """
        |INSERT INTO PracticalCourse(
        |    studentId,
        |    termYear,
        |    termIndex,
        |    courseName,
        |    weekStr,
        |    weekList,
        |    credit,
        |    teacher
        |) VALUES (
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?
        |)
        """.trimMargin(), 8) {
          bindString(0, studentId)
          bindLong(1, termYear)
          bindLong(2, termIndex)
          bindString(3, courseName)
          bindString(4, weekStr)
          bindString(5, weekList)
          bindDouble(6, credit)
          bindString(7, teacher)
        }
    notifyQueries(1_183_393_808) { emit ->
      emit("PracticalCourse")
    }
  }

  public fun updatePracticalCourseById(
    courseName: String,
    weekStr: String,
    weekList: String,
    credit: Double,
    teacher: String,
    id: Long,
  ) {
    driver.execute(1_286_905_842, """
        |UPDATE PracticalCourse SET
        |    courseName = ?,
        |    weekStr = ?,
        |    weekList = ?,
        |    credit = ?,
        |    teacher = ?
        |WHERE id = ?
        """.trimMargin(), 6) {
          bindString(0, courseName)
          bindString(1, weekStr)
          bindString(2, weekList)
          bindDouble(3, credit)
          bindString(4, teacher)
          bindLong(5, id)
        }
    notifyQueries(1_286_905_842) { emit ->
      emit("PracticalCourse")
    }
  }

  public fun deletePracticalCourseById(id: Long) {
    driver.execute(487_723_984, """
        |DELETE FROM PracticalCourse
        |WHERE id = ?
        """.trimMargin(), 1) {
          bindLong(0, id)
        }
    notifyQueries(487_723_984) { emit ->
      emit("PracticalCourse")
    }
  }

  public fun deletePracticalCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ) {
    driver.execute(-503_541_826, """
        |DELETE FROM PracticalCourse
        |WHERE studentId = ? AND termYear = ? AND termIndex = ?
        """.trimMargin(), 3) {
          bindString(0, studentId)
          bindLong(1, termYear)
          bindLong(2, termIndex)
        }
    notifyQueries(-503_541_826) { emit ->
      emit("PracticalCourse")
    }
  }

  public fun upsertCustomCourse(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    courseId: Long,
    courseName: String,
    weekStr: String,
    weekList: String,
    day: Long,
    dayIndex: Long,
    startDayTime: Long,
    endDayTime: Long,
    startTime: String,
    endTime: String,
    location: String,
    teacher: String,
    extraData: String,
    createTime: Long,
  ) {
    driver.execute(-686_642_462, """
        |INSERT OR REPLACE INTO CustomCourse(
        |    studentId,
        |    termYear,
        |    termIndex,
        |    courseId,
        |    courseName,
        |    weekStr,
        |    weekList,
        |    day,
        |    dayIndex,
        |    startDayTime,
        |    endDayTime,
        |    startTime,
        |    endTime,
        |    location,
        |    teacher,
        |    extraData,
        |    createTime
        |) VALUES (
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?
        |)
        """.trimMargin(), 17) {
          bindString(0, studentId)
          bindLong(1, termYear)
          bindLong(2, termIndex)
          bindLong(3, courseId)
          bindString(4, courseName)
          bindString(5, weekStr)
          bindString(6, weekList)
          bindLong(7, day)
          bindLong(8, dayIndex)
          bindLong(9, startDayTime)
          bindLong(10, endDayTime)
          bindString(11, startTime)
          bindString(12, endTime)
          bindString(13, location)
          bindString(14, teacher)
          bindString(15, extraData)
          bindLong(16, createTime)
        }
    notifyQueries(-686_642_462) { emit ->
      emit("CustomCourse")
    }
  }

  public fun deleteCustomCourseById(
    studentId: String,
    termYear: Long,
    termIndex: Long,
    courseId: Long,
  ) {
    driver.execute(1_720_528_464, """
        |DELETE FROM CustomCourse
        |WHERE studentId = ? AND termYear = ? AND termIndex = ? AND courseId = ?
        """.trimMargin(), 4) {
          bindString(0, studentId)
          bindLong(1, termYear)
          bindLong(2, termIndex)
          bindLong(3, courseId)
        }
    notifyQueries(1_720_528_464) { emit ->
      emit("CustomCourse")
    }
  }

  public fun deleteCustomCoursesByPartition(
    studentId: String,
    termYear: Long,
    termIndex: Long,
  ) {
    driver.execute(-1_779_094_978, """
        |DELETE FROM CustomCourse
        |WHERE studentId = ? AND termYear = ? AND termIndex = ?
        """.trimMargin(), 3) {
          bindString(0, studentId)
          bindLong(1, termYear)
          bindLong(2, termIndex)
        }
    notifyQueries(-1_779_094_978) { emit ->
      emit("CustomCourse")
    }
  }

  public fun upsertCustomThing(
    studentId: String,
    thingId: Long,
    title: String,
    location: String,
    allDay: Long,
    startTime: Long,
    endTime: Long,
    remark: String,
    color: String,
    metadata: String,
    createTime: Long,
  ) {
    driver.execute(-976_501_433, """
        |INSERT OR REPLACE INTO CustomThing(
        |    studentId,
        |    thingId,
        |    title,
        |    location,
        |    allDay,
        |    startTime,
        |    endTime,
        |    remark,
        |    color,
        |    metadata,
        |    createTime
        |) VALUES (
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?
        |)
        """.trimMargin(), 11) {
          bindString(0, studentId)
          bindLong(1, thingId)
          bindString(2, title)
          bindString(3, location)
          bindLong(4, allDay)
          bindLong(5, startTime)
          bindLong(6, endTime)
          bindString(7, remark)
          bindString(8, color)
          bindString(9, metadata)
          bindLong(10, createTime)
        }
    notifyQueries(-976_501_433) { emit ->
      emit("CustomThing")
    }
  }

  public fun deleteCustomThingById(studentId: String, thingId: Long) {
    driver.execute(-999_545_411, """
        |DELETE FROM CustomThing
        |WHERE studentId = ? AND thingId = ?
        """.trimMargin(), 2) {
          bindString(0, studentId)
          bindLong(1, thingId)
        }
    notifyQueries(-999_545_411) { emit ->
      emit("CustomThing")
    }
  }

  public fun deleteCustomThingsByStudent(studentId: String) {
    driver.execute(-1_026_469_828, """
        |DELETE FROM CustomThing
        |WHERE studentId = ?
        """.trimMargin(), 1) {
          bindString(0, studentId)
        }
    notifyQueries(-1_026_469_828) { emit ->
      emit("CustomThing")
    }
  }

  public fun deleteAllDataByStudent(studentId: String) {
    driver.execute(108_500_651, """DELETE FROM SelectedTerm WHERE studentId = ?""", 1) {
          bindString(0, studentId)
        }
    notifyQueries(108_500_651) { emit ->
      emit("SelectedTerm")
    }
  }

  public fun deleteAllSyncStateByStudent(studentId: String) {
    driver.execute(1_653_279_005, """DELETE FROM SyncState WHERE studentId = ?""", 1) {
          bindString(0, studentId)
        }
    notifyQueries(1_653_279_005) { emit ->
      emit("SyncState")
    }
  }

  public fun deleteAllCoursesByStudent(studentId: String) {
    driver.execute(-121_040_357, """DELETE FROM Course WHERE studentId = ?""", 1) {
          bindString(0, studentId)
        }
    notifyQueries(-121_040_357) { emit ->
      emit("Course")
    }
  }

  public fun deleteAllExperimentCoursesByStudent(studentId: String) {
    driver.execute(-1_480_797_832, """DELETE FROM ExperimentCourse WHERE studentId = ?""", 1) {
          bindString(0, studentId)
        }
    notifyQueries(-1_480_797_832) { emit ->
      emit("ExperimentCourse")
    }
  }

  public fun deleteAllPracticalCoursesByStudent(studentId: String) {
    driver.execute(-71_188_206, """DELETE FROM PracticalCourse WHERE studentId = ?""", 1) {
          bindString(0, studentId)
        }
    notifyQueries(-71_188_206) { emit ->
      emit("PracticalCourse")
    }
  }

  public fun deleteAllCustomCoursesByStudent(studentId: String) {
    driver.execute(-833_644_436, """DELETE FROM CustomCourse WHERE studentId = ?""", 1) {
          bindString(0, studentId)
        }
    notifyQueries(-833_644_436) { emit ->
      emit("CustomCourse")
    }
  }

  public fun upsertSetting(
    scope: String,
    scopeId: String,
    name: String,
    `value`: String,
    updatedAt: Long,
  ) {
    driver.execute(-1_900_773_222, """
        |INSERT OR REPLACE INTO Setting(scope, scopeId, name, value, updatedAt)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          bindString(0, scope)
          bindString(1, scopeId)
          bindString(2, name)
          bindString(3, value)
          bindLong(4, updatedAt)
        }
    notifyQueries(-1_900_773_222) { emit ->
      emit("Setting")
    }
  }

  public fun deleteSetting(
    scope: String,
    scopeId: String,
    name: String,
  ) {
    driver.execute(1_668_632_734, """
        |DELETE FROM Setting
        |WHERE scope = ? AND scopeId = ? AND name = ?
        """.trimMargin(), 3) {
          bindString(0, scope)
          bindString(1, scopeId)
          bindString(2, name)
        }
    notifyQueries(1_668_632_734) { emit ->
      emit("Setting")
    }
  }

  public fun deleteSettingsByScope(scope: String, scopeId: String) {
    driver.execute(606_008_040, """
        |DELETE FROM Setting
        |WHERE scope = ? AND scopeId = ?
        """.trimMargin(), 2) {
          bindString(0, scope)
          bindString(1, scopeId)
        }
    notifyQueries(606_008_040) { emit ->
      emit("Setting")
    }
  }

  public fun upsertCourseColor(
    studentId: String,
    courseName: String,
    colorHex: String,
    updatedAt: Long,
  ) {
    driver.execute(702_510_418, """
        |INSERT OR REPLACE INTO CourseColor(studentId, courseName, colorHex, updatedAt)
        |VALUES (?, ?, ?, ?)
        """.trimMargin(), 4) {
          bindString(0, studentId)
          bindString(1, courseName)
          bindString(2, colorHex)
          bindLong(3, updatedAt)
        }
    notifyQueries(702_510_418) { emit ->
      emit("CourseColor")
    }
  }

  public fun deleteCourseColor(studentId: String, courseName: String) {
    driver.execute(300_983_126, """
        |DELETE FROM CourseColor
        |WHERE studentId = ? AND courseName = ?
        """.trimMargin(), 2) {
          bindString(0, studentId)
          bindString(1, courseName)
        }
    notifyQueries(300_983_126) { emit ->
      emit("CourseColor")
    }
  }

  public fun deleteCourseColorsByStudent(studentId: String) {
    driver.execute(199_011_975, """
        |DELETE FROM CourseColor
        |WHERE studentId = ?
        """.trimMargin(), 1) {
          bindString(0, studentId)
        }
    notifyQueries(199_011_975) { emit ->
      emit("CourseColor")
    }
  }

  public fun upsertNotice(
    noticeId: Long?,
    title: String,
    content: String,
    actionsJson: String,
    released: Long,
    createTime: Long,
    updateTime: Long,
  ) {
    driver.execute(1_467_331_406, """
        |INSERT OR REPLACE INTO Notice(noticeId, title, content, actionsJson, released, createTime, updateTime)
        |VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 7) {
          bindLong(0, noticeId)
          bindString(1, title)
          bindString(2, content)
          bindString(3, actionsJson)
          bindLong(4, released)
          bindLong(5, createTime)
          bindLong(6, updateTime)
        }
    notifyQueries(1_467_331_406) { emit ->
      emit("Notice")
    }
  }

  public fun deleteNoticeById(noticeId: Long) {
    driver.execute(-979_489_348, """
        |DELETE FROM Notice
        |WHERE noticeId = ?
        """.trimMargin(), 1) {
          bindLong(0, noticeId)
        }
    notifyQueries(-979_489_348) { emit ->
      emit("Notice")
    }
  }

  public fun deleteAllNotices() {
    driver.execute(-484_217_172, """DELETE FROM Notice""", 0)
    notifyQueries(-484_217_172) { emit ->
      emit("Notice")
    }
  }

  public fun upsertBackground(
    backgroundId: Long?,
    resourceId: Long,
    thumbnailUrl: String,
    imageUrl: String,
    updatedAt: Long,
  ) {
    driver.execute(1_880_573_604, """
        |INSERT OR REPLACE INTO Background(backgroundId, resourceId, thumbnailUrl, imageUrl, updatedAt)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          bindLong(0, backgroundId)
          bindLong(1, resourceId)
          bindString(2, thumbnailUrl)
          bindString(3, imageUrl)
          bindLong(4, updatedAt)
        }
    notifyQueries(1_880_573_604) { emit ->
      emit("Background")
    }
  }

  public fun deleteBackgroundById(backgroundId: Long) {
    driver.execute(1_466_238_994, """
        |DELETE FROM Background
        |WHERE backgroundId = ?
        """.trimMargin(), 1) {
          bindLong(0, backgroundId)
        }
    notifyQueries(1_466_238_994) { emit ->
      emit("Background")
    }
  }

  public fun deleteAllBackgrounds() {
    driver.execute(997_526_166, """DELETE FROM Background""", 0)
    notifyQueries(997_526_166) { emit ->
      emit("Background")
    }
  }

  private inner class SelectUserQuery<out T : Any>(
    public val studentId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("User", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("User", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(134_814_478, """
    |SELECT User.studentId, User.tokenEncrypted, User.name, User.gender, User.xhuGrade, User.college, User.majorName, User.className, User.majorDirection FROM User
    |WHERE studentId = ?
    """.trimMargin(), mapper, 1) {
      bindString(0, studentId)
    }

    override fun toString(): String = "Schema.sq:selectUser"
  }

  private inner class SelectSelectedTermQuery<out T : Any>(
    public val studentId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("SelectedTerm", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("SelectedTerm", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(193_536_490, """
    |SELECT SelectedTerm.studentId, SelectedTerm.termYear, SelectedTerm.termIndex FROM SelectedTerm
    |WHERE studentId = ?
    """.trimMargin(), mapper, 1) {
      bindString(0, studentId)
    }

    override fun toString(): String = "Schema.sq:selectSelectedTerm"
  }

  private inner class SelectSyncStateQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("SyncState", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("SyncState", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-833_026_733, """
    |SELECT SyncState.studentId, SyncState.termYear, SyncState.termIndex, SyncState.lastSyncAt FROM SyncState
    |WHERE studentId = ? AND termYear = ? AND termIndex = ?
    """.trimMargin(), mapper, 3) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
    }

    override fun toString(): String = "Schema.sq:selectSyncState"
  }

  private inner class SelectLastSyncAtQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("SyncState", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("SyncState", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-212_398_233, """
    |SELECT lastSyncAt FROM SyncState
    |WHERE studentId = ? AND termYear = ? AND termIndex = ?
    """.trimMargin(), mapper, 3) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
    }

    override fun toString(): String = "Schema.sq:selectLastSyncAt"
  }

  private inner class SelectCoursesByPartitionQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Course", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Course", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_045_801_090, """
    |SELECT Course.id, Course.studentId, Course.termYear, Course.termIndex, Course.courseName, Course.weekStr, Course.weekList, Course.day, Course.dayIndex, Course.startDayTime, Course.endDayTime, Course.startTime, Course.endTime, Course.location, Course.teacher, Course.extraData, Course.credit, Course.courseType, Course.courseCodeType, Course.courseCodeFlag, Course.campus FROM Course
    |WHERE studentId = ? AND termYear = ? AND termIndex = ?
    |ORDER BY dayIndex, startDayTime, startTime
    """.trimMargin(), mapper, 3) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
    }

    override fun toString(): String = "Schema.sq:selectCoursesByPartition"
  }

  private inner class SelectCoursesByPartitionAndDayQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    public val dayIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Course", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Course", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(382_065_507, """
    |SELECT Course.id, Course.studentId, Course.termYear, Course.termIndex, Course.courseName, Course.weekStr, Course.weekList, Course.day, Course.dayIndex, Course.startDayTime, Course.endDayTime, Course.startTime, Course.endTime, Course.location, Course.teacher, Course.extraData, Course.credit, Course.courseType, Course.courseCodeType, Course.courseCodeFlag, Course.campus FROM Course
    |WHERE studentId = ? AND termYear = ? AND termIndex = ? AND dayIndex = ?
    |ORDER BY startDayTime, startTime
    """.trimMargin(), mapper, 4) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
      bindLong(3, dayIndex)
    }

    override fun toString(): String = "Schema.sq:selectCoursesByPartitionAndDay"
  }

  private inner class SelectCourseByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Course", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Course", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(333_683_600, """
    |SELECT Course.id, Course.studentId, Course.termYear, Course.termIndex, Course.courseName, Course.weekStr, Course.weekList, Course.day, Course.dayIndex, Course.startDayTime, Course.endDayTime, Course.startTime, Course.endTime, Course.location, Course.teacher, Course.extraData, Course.credit, Course.courseType, Course.courseCodeType, Course.courseCodeFlag, Course.campus FROM Course
    |WHERE id = ?
    """.trimMargin(), mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "Schema.sq:selectCourseById"
  }

  private inner class CountCoursesByPartitionQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Course", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Course", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-464_653_277, """
    |SELECT COUNT(*) FROM Course
    |WHERE studentId = ? AND termYear = ? AND termIndex = ?
    """.trimMargin(), mapper, 3) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
    }

    override fun toString(): String = "Schema.sq:countCoursesByPartition"
  }

  private inner class SelectExperimentCoursesByPartitionQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("ExperimentCourse", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("ExperimentCourse", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_871_551_845, """
    |SELECT ExperimentCourse.id, ExperimentCourse.studentId, ExperimentCourse.termYear, ExperimentCourse.termIndex, ExperimentCourse.courseName, ExperimentCourse.experimentProjectName, ExperimentCourse.experimentGroupName, ExperimentCourse.weekStr, ExperimentCourse.weekList, ExperimentCourse.day, ExperimentCourse.dayIndex, ExperimentCourse.startDayTime, ExperimentCourse.endDayTime, ExperimentCourse.startTime, ExperimentCourse.endTime, ExperimentCourse.location, ExperimentCourse.teacherName, ExperimentCourse.region FROM ExperimentCourse
    |WHERE studentId = ? AND termYear = ? AND termIndex = ?
    |ORDER BY dayIndex, startDayTime, startTime
    """.trimMargin(), mapper, 3) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
    }

    override fun toString(): String = "Schema.sq:selectExperimentCoursesByPartition"
  }

  private inner class SelectExperimentCoursesByPartitionAndDayQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    public val dayIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("ExperimentCourse", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("ExperimentCourse", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_842_455_360, """
    |SELECT ExperimentCourse.id, ExperimentCourse.studentId, ExperimentCourse.termYear, ExperimentCourse.termIndex, ExperimentCourse.courseName, ExperimentCourse.experimentProjectName, ExperimentCourse.experimentGroupName, ExperimentCourse.weekStr, ExperimentCourse.weekList, ExperimentCourse.day, ExperimentCourse.dayIndex, ExperimentCourse.startDayTime, ExperimentCourse.endDayTime, ExperimentCourse.startTime, ExperimentCourse.endTime, ExperimentCourse.location, ExperimentCourse.teacherName, ExperimentCourse.region FROM ExperimentCourse
    |WHERE studentId = ? AND termYear = ? AND termIndex = ? AND dayIndex = ?
    |ORDER BY startDayTime, startTime
    """.trimMargin(), mapper, 4) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
      bindLong(3, dayIndex)
    }

    override fun toString(): String = "Schema.sq:selectExperimentCoursesByPartitionAndDay"
  }

  private inner class SelectExperimentCourseByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("ExperimentCourse", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("ExperimentCourse", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_910_707_117, """
    |SELECT ExperimentCourse.id, ExperimentCourse.studentId, ExperimentCourse.termYear, ExperimentCourse.termIndex, ExperimentCourse.courseName, ExperimentCourse.experimentProjectName, ExperimentCourse.experimentGroupName, ExperimentCourse.weekStr, ExperimentCourse.weekList, ExperimentCourse.day, ExperimentCourse.dayIndex, ExperimentCourse.startDayTime, ExperimentCourse.endDayTime, ExperimentCourse.startTime, ExperimentCourse.endTime, ExperimentCourse.location, ExperimentCourse.teacherName, ExperimentCourse.region FROM ExperimentCourse
    |WHERE id = ?
    """.trimMargin(), mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "Schema.sq:selectExperimentCourseById"
  }

  private inner class CountExperimentCoursesByPartitionQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("ExperimentCourse", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("ExperimentCourse", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_470_772_480, """
    |SELECT COUNT(*) FROM ExperimentCourse
    |WHERE studentId = ? AND termYear = ? AND termIndex = ?
    """.trimMargin(), mapper, 3) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
    }

    override fun toString(): String = "Schema.sq:countExperimentCoursesByPartition"
  }

  private inner class SelectPracticalCoursesByPartitionQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("PracticalCourse", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("PracticalCourse", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_617_832_499, """
    |SELECT PracticalCourse.id, PracticalCourse.studentId, PracticalCourse.termYear, PracticalCourse.termIndex, PracticalCourse.courseName, PracticalCourse.weekStr, PracticalCourse.weekList, PracticalCourse.credit, PracticalCourse.teacher FROM PracticalCourse
    |WHERE studentId = ? AND termYear = ? AND termIndex = ?
    |ORDER BY courseName
    """.trimMargin(), mapper, 3) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
    }

    override fun toString(): String = "Schema.sq:selectPracticalCoursesByPartition"
  }

  private inner class SelectPracticalCourseByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("PracticalCourse", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("PracticalCourse", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_741_039_393, """
    |SELECT PracticalCourse.id, PracticalCourse.studentId, PracticalCourse.termYear, PracticalCourse.termIndex, PracticalCourse.courseName, PracticalCourse.weekStr, PracticalCourse.weekList, PracticalCourse.credit, PracticalCourse.teacher FROM PracticalCourse
    |WHERE id = ?
    """.trimMargin(), mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "Schema.sq:selectPracticalCourseById"
  }

  private inner class CountPracticalCoursesByPartitionQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("PracticalCourse", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("PracticalCourse", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-124_542_264, """
    |SELECT COUNT(*) FROM PracticalCourse
    |WHERE studentId = ? AND termYear = ? AND termIndex = ?
    """.trimMargin(), mapper, 3) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
    }

    override fun toString(): String = "Schema.sq:countPracticalCoursesByPartition"
  }

  private inner class SelectCustomCoursesByPartitionQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CustomCourse", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CustomCourse", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_064_908_431, """
    |SELECT CustomCourse.studentId, CustomCourse.termYear, CustomCourse.termIndex, CustomCourse.courseId, CustomCourse.courseName, CustomCourse.weekStr, CustomCourse.weekList, CustomCourse.day, CustomCourse.dayIndex, CustomCourse.startDayTime, CustomCourse.endDayTime, CustomCourse.startTime, CustomCourse.endTime, CustomCourse.location, CustomCourse.teacher, CustomCourse.extraData, CustomCourse.createTime FROM CustomCourse
    |WHERE studentId = ? AND termYear = ? AND termIndex = ?
    |ORDER BY dayIndex, startDayTime, startTime
    """.trimMargin(), mapper, 3) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
    }

    override fun toString(): String = "Schema.sq:selectCustomCoursesByPartition"
  }

  private inner class SelectCustomCoursesByPartitionAndDayQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    public val dayIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CustomCourse", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CustomCourse", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_829_017_164, """
    |SELECT CustomCourse.studentId, CustomCourse.termYear, CustomCourse.termIndex, CustomCourse.courseId, CustomCourse.courseName, CustomCourse.weekStr, CustomCourse.weekList, CustomCourse.day, CustomCourse.dayIndex, CustomCourse.startDayTime, CustomCourse.endDayTime, CustomCourse.startTime, CustomCourse.endTime, CustomCourse.location, CustomCourse.teacher, CustomCourse.extraData, CustomCourse.createTime FROM CustomCourse
    |WHERE studentId = ? AND termYear = ? AND termIndex = ? AND dayIndex = ?
    |ORDER BY startDayTime, startTime
    """.trimMargin(), mapper, 4) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
      bindLong(3, dayIndex)
    }

    override fun toString(): String = "Schema.sq:selectCustomCoursesByPartitionAndDay"
  }

  private inner class SelectCustomCourseByIdQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    public val courseId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CustomCourse", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CustomCourse", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-190_375_007, """
    |SELECT CustomCourse.studentId, CustomCourse.termYear, CustomCourse.termIndex, CustomCourse.courseId, CustomCourse.courseName, CustomCourse.weekStr, CustomCourse.weekList, CustomCourse.day, CustomCourse.dayIndex, CustomCourse.startDayTime, CustomCourse.endDayTime, CustomCourse.startTime, CustomCourse.endTime, CustomCourse.location, CustomCourse.teacher, CustomCourse.extraData, CustomCourse.createTime FROM CustomCourse
    |WHERE studentId = ? AND termYear = ? AND termIndex = ? AND courseId = ?
    """.trimMargin(), mapper, 4) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
      bindLong(3, courseId)
    }

    override fun toString(): String = "Schema.sq:selectCustomCourseById"
  }

  private inner class CountCustomCoursesByPartitionQuery<out T : Any>(
    public val studentId: String,
    public val termYear: Long,
    public val termIndex: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CustomCourse", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CustomCourse", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(6_030_196, """
    |SELECT COUNT(*) FROM CustomCourse
    |WHERE studentId = ? AND termYear = ? AND termIndex = ?
    """.trimMargin(), mapper, 3) {
      bindString(0, studentId)
      bindLong(1, termYear)
      bindLong(2, termIndex)
    }

    override fun toString(): String = "Schema.sq:countCustomCoursesByPartition"
  }

  private inner class SelectCustomThingsByStudentQuery<out T : Any>(
    public val studentId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CustomThing", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CustomThing", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(320_605_579, """
    |SELECT CustomThing.studentId, CustomThing.thingId, CustomThing.title, CustomThing.location, CustomThing.allDay, CustomThing.startTime, CustomThing.endTime, CustomThing.remark, CustomThing.color, CustomThing.metadata, CustomThing.createTime FROM CustomThing
    |WHERE studentId = ?
    |ORDER BY startTime, endTime
    """.trimMargin(), mapper, 1) {
      bindString(0, studentId)
    }

    override fun toString(): String = "Schema.sq:selectCustomThingsByStudent"
  }

  private inner class SelectCustomThingByIdQuery<out T : Any>(
    public val studentId: String,
    public val thingId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CustomThing", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CustomThing", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_571_211_852, """
    |SELECT CustomThing.studentId, CustomThing.thingId, CustomThing.title, CustomThing.location, CustomThing.allDay, CustomThing.startTime, CustomThing.endTime, CustomThing.remark, CustomThing.color, CustomThing.metadata, CustomThing.createTime FROM CustomThing
    |WHERE studentId = ? AND thingId = ?
    """.trimMargin(), mapper, 2) {
      bindString(0, studentId)
      bindLong(1, thingId)
    }

    override fun toString(): String = "Schema.sq:selectCustomThingById"
  }

  private inner class SelectSettingQuery<out T : Any>(
    public val scope: String,
    public val scopeId: String,
    public val name: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Setting", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Setting", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_698_106_323, """
    |SELECT value FROM Setting
    |WHERE scope = ? AND scopeId = ? AND name = ?
    """.trimMargin(), mapper, 3) {
      bindString(0, scope)
      bindString(1, scopeId)
      bindString(2, name)
    }

    override fun toString(): String = "Schema.sq:selectSetting"
  }

  private inner class SelectSettingsByScopeQuery<out T : Any>(
    public val scope: String,
    public val scopeId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Setting", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Setting", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_118_201_993, """
    |SELECT Setting.scope, Setting.scopeId, Setting.name, Setting.value, Setting.updatedAt FROM Setting
    |WHERE scope = ? AND scopeId = ?
    """.trimMargin(), mapper, 2) {
      bindString(0, scope)
      bindString(1, scopeId)
    }

    override fun toString(): String = "Schema.sq:selectSettingsByScope"
  }

  private inner class SelectCourseColorQuery<out T : Any>(
    public val studentId: String,
    public val courseName: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CourseColor", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CourseColor", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_754_916_709, """
    |SELECT colorHex FROM CourseColor
    |WHERE studentId = ? AND courseName = ?
    """.trimMargin(), mapper, 2) {
      bindString(0, studentId)
      bindString(1, courseName)
    }

    override fun toString(): String = "Schema.sq:selectCourseColor"
  }

  private inner class SelectCourseColorsByStudentQuery<out T : Any>(
    public val studentId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CourseColor", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CourseColor", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_546_087_382, """
    |SELECT CourseColor.studentId, CourseColor.courseName, CourseColor.colorHex, CourseColor.updatedAt FROM CourseColor
    |WHERE studentId = ?
    """.trimMargin(), mapper, 1) {
      bindString(0, studentId)
    }

    override fun toString(): String = "Schema.sq:selectCourseColorsByStudent"
  }

  private inner class SelectNoticeByIdQuery<out T : Any>(
    public val noticeId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Notice", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Notice", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(868_527_053, """
    |SELECT Notice.noticeId, Notice.title, Notice.content, Notice.actionsJson, Notice.released, Notice.createTime, Notice.updateTime FROM Notice
    |WHERE noticeId = ?
    """.trimMargin(), mapper, 1) {
      bindLong(0, noticeId)
    }

    override fun toString(): String = "Schema.sq:selectNoticeById"
  }

  private inner class SelectNoticesPagedQuery<out T : Any>(
    public val limit: Long,
    public val offset: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Notice", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Notice", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_498_439_651, """
    |SELECT Notice.noticeId, Notice.title, Notice.content, Notice.actionsJson, Notice.released, Notice.createTime, Notice.updateTime FROM Notice
    |ORDER BY updateTime DESC
    |LIMIT ? OFFSET ?
    """.trimMargin(), mapper, 2) {
      bindLong(0, limit)
      bindLong(1, offset)
    }

    override fun toString(): String = "Schema.sq:selectNoticesPaged"
  }

  private inner class SelectBackgroundByIdQuery<out T : Any>(
    public val backgroundId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Background", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Background", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(856_429_987, """
    |SELECT Background.backgroundId, Background.resourceId, Background.thumbnailUrl, Background.imageUrl, Background.updatedAt FROM Background
    |WHERE backgroundId = ?
    """.trimMargin(), mapper, 1) {
      bindLong(0, backgroundId)
    }

    override fun toString(): String = "Schema.sq:selectBackgroundById"
  }
}
