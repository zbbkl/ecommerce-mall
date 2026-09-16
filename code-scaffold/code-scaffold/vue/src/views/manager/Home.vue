<template>
  <div>
    <el-row :gutter="24">
      <el-col :span="24">
        <el-card style="margin: 10px 0">
          <div style="font-size: 17px;font-weight: 700">大家好，欢迎使用本系统！</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "Home",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
    }
  },
  created() {},
  methods: {}
}
</script>

<style scoped>
</style>
